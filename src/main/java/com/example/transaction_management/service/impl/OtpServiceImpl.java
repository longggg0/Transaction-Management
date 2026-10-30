package com.example.transaction_management.service.impl;

import com.example.transaction_management.common.enums.OtpPurpose;
import com.example.transaction_management.common.errors.ErrorCode;
import com.example.transaction_management.common.exceptions.BusinessException;
import com.example.transaction_management.dto.request.ResendOtpRequest;
import com.example.transaction_management.dto.request.VerifyOtpRequest;
import com.example.transaction_management.dto.response.MessageResponse;
import com.example.transaction_management.entity.OtpCodeEntity;
import com.example.transaction_management.entity.UserEntity;
import com.example.transaction_management.repository.OtpCodeRepository;
import com.example.transaction_management.repository.UserRepository;
import com.example.transaction_management.service.OtpSenderService;
import com.example.transaction_management.service.OtpService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class OtpServiceImpl implements OtpService {

    private static final int EXPIRY_MINUTES = 5;
    private static final int MAX_ATTEMPTS = 5;
    private static final int RESEND_SECONDS = 60;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final OtpCodeRepository otpCodeRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final OtpSenderService otpSender;

    @Override
    @Transactional
    public void sendRegisterOtp(UserEntity user) {
        // invalidate the previous unused code, then issue a new one
        otpCodeRepository
                .findFirstByUserIdAndPurposeAndUsedFalseOrderByCreatedAtDesc(user.getId(), OtpPurpose.REGISTER)
                .ifPresent(old -> old.setUsed(true));

        String code = String.format("%06d", RANDOM.nextInt(1_000_000));

        OtpCodeEntity otp = new OtpCodeEntity();
        otp.setUserId(user.getId());
        otp.setCodeHash(passwordEncoder.encode(code));
        otp.setPurpose(OtpPurpose.REGISTER);
        otp.setExpiresAt(LocalDateTime.now().plusMinutes(EXPIRY_MINUTES));
        otpCodeRepository.save(otp);

        otpSender.send(user.getEmail(), code);
    }

    // noRollbackFor: the "attempts + 1" must be saved even when we throw
    @Override
    @Transactional(noRollbackFor = BusinessException.class)
    public MessageResponse verifyRegisterOtp(VerifyOtpRequest request) {
        UserEntity user = userRepository.findByEmail(request.getEmail().trim().toLowerCase())
                .orElseThrow(() -> new BusinessException(ErrorCode.OTP_INVALID));

        if (user.isEnabled()) {
            throw new BusinessException(ErrorCode.OTP_INVALID);
        }

        OtpCodeEntity otp = otpCodeRepository
                .findFirstByUserIdAndPurposeAndUsedFalseOrderByCreatedAtDesc(user.getId(), OtpPurpose.REGISTER)
                .orElseThrow(() -> new BusinessException(ErrorCode.OTP_INVALID));

        if (otp.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BusinessException(ErrorCode.OTP_EXPIRED);
        }
        if (otp.getAttempts() >= MAX_ATTEMPTS) {
            throw new BusinessException(ErrorCode.OTP_TOO_MANY_ATTEMPTS);
        }
        if (!passwordEncoder.matches(request.getCode(), otp.getCodeHash())) {
            otp.setAttempts(otp.getAttempts() + 1);
            throw new BusinessException(ErrorCode.OTP_INVALID);
        }

        otp.setUsed(true);
        user.setEnabled(true);
        return new MessageResponse("Account verified. You can log in now.");
    }

    @Override
    @Transactional
    public MessageResponse resendRegisterOtp(ResendOtpRequest request) {
        MessageResponse response = new MessageResponse("If the account exists, a new code was sent.");

        UserEntity user = userRepository.findByEmail(request.getEmail().trim().toLowerCase()).orElse(null);
        if (user == null || user.isEnabled()) {
            return response;      // same answer, so nobody can probe which emails exist
        }

        otpCodeRepository
                .findFirstByUserIdAndPurposeOrderByCreatedAtDesc(user.getId(), OtpPurpose.REGISTER)
                .filter(last -> last.getCreatedAt().isAfter(LocalDateTime.now().minusSeconds(RESEND_SECONDS)))
                .ifPresent(last -> { throw new BusinessException(ErrorCode.OTP_RESEND_TOO_SOON); });

        sendRegisterOtp(user);
        return response;
    }
}

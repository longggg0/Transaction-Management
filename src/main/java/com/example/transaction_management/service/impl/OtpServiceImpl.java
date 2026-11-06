package com.example.transaction_management.service.impl;

import com.example.transaction_management.common.enums.OtpPurpose;
import com.example.transaction_management.common.enums.Role;
import com.example.transaction_management.common.errors.ErrorCode;
import com.example.transaction_management.common.exceptions.BusinessException;
import com.example.transaction_management.dto.request.ResendOtpRequest;
import com.example.transaction_management.dto.request.ResetPasswordRequest;
import com.example.transaction_management.dto.request.VerifyOtpRequest;
import com.example.transaction_management.entity.OtpCodeEntity;
import com.example.transaction_management.entity.PendingRegistrationEntity;
import com.example.transaction_management.entity.UserEntity;
import com.example.transaction_management.repository.OtpCodeRepository;
import com.example.transaction_management.repository.PendingRegistrationRepository;
import com.example.transaction_management.repository.UserRepository;
import com.example.transaction_management.service.OtpSenderService;
import com.example.transaction_management.service.OtpService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OtpServiceImpl implements OtpService {

    private static final int EXPIRY_MINUTES = 5;
    private static final int MAX_ATTEMPTS = 5;
    private static final int RESEND_SECONDS = 60;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final OtpCodeRepository otpCodeRepository;
    private final PendingRegistrationRepository pendingRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final OtpSenderService otpSender;

    @Override
    @Transactional
    public void sendRegisterOtp(PendingRegistrationEntity pending) {
        LocalDateTime now = LocalDateTime.now();

        // cooldown only applies to a pending row that already exists
        if (pending.getId() != null && pending.getSentAt() != null
                && pending.getSentAt().isAfter(now.minusSeconds(RESEND_SECONDS))) {
            throw new BusinessException(ErrorCode.OTP_RESEND_TOO_SOON);
        }

        String code = generateCode();
        pending.setCodeHash(passwordEncoder.encode(code));
        pending.setAttempts(0);
        pending.setExpiresAt(now.plusMinutes(EXPIRY_MINUTES));
        pending.setSentAt(now);
        pendingRepository.save(pending);

        otpSender.send(pending.getEmail(), code);   // if this fails, the save is rolled back
    }

    @Override
    @Transactional(noRollbackFor = BusinessException.class)
    public String verifyRegisterOtp(VerifyOtpRequest request) {
        String email = request.getEmail().trim().toLowerCase();

        PendingRegistrationEntity pending = pendingRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.OTP_INVALID));

        if (pending.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BusinessException(ErrorCode.OTP_EXPIRED);
        }
        if (pending.getAttempts() >= MAX_ATTEMPTS) {
            throw new BusinessException(ErrorCode.OTP_TOO_MANY_ATTEMPTS);
        }
        if (!passwordEncoder.matches(request.getCode(), pending.getCodeHash())) {
            pending.setAttempts(pending.getAttempts() + 1);
            throw new BusinessException(ErrorCode.OTP_INVALID);
        }

        // someone may have taken the email or username while this was pending
        if (userRepository.existsByEmail(email)) {
            pendingRepository.delete(pending);
            throw new BusinessException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }
        if (userRepository.existsByUsername(pending.getUsername())) {
            pendingRepository.delete(pending);
            throw new BusinessException(ErrorCode.USERNAME_ALREADY_EXISTS);
        }

        UserEntity user = new UserEntity();
        user.setEmail(pending.getEmail());
        user.setUsername(pending.getUsername());
        user.setPassword(pending.getPasswordHash());    // already BCrypt-hashed
        user.setRole(Role.USER);
        user.setEnabled(true);
        userRepository.save(user);

        pendingRepository.delete(pending);
        return ("Account created. You can log in now.");
    }

    @Override
    @Transactional
    public String resendRegisterOtp(ResendOtpRequest request) {

        String email = request.getEmail().trim().toLowerCase();

        pendingRepository.findByEmail(email)
                .ifPresent(pending -> {
                    try {
                        sendRegisterOtp(pending);
                    } catch (BusinessException ex) {
                        if (ex.getErrorCode() != ErrorCode.OTP_RESEND_TOO_SOON) {
                            throw ex;
                        }
                    }
                });

        return "If a registration is pending, a new code was sent.";
    }

    @Override
    @Transactional
    public void sendForgotPasswordOtp(UserEntity user) {

        // Check resend cooldown
        Optional<OtpCodeEntity> latestOtp =
                otpCodeRepository
                        .findFirstByUserIdAndPurposeOrderByCreatedAtDesc(
                                user.getId(),
                                OtpPurpose.FORGOT_PASSWORD
                        );

        if (latestOtp.isPresent()) {
            LocalDateTime resendAvailableAt =
                    latestOtp.get().getCreatedAt()
                            .plusSeconds(RESEND_SECONDS);

            if (LocalDateTime.now().isBefore(resendAvailableAt)) {
                throw new BusinessException(
                        ErrorCode.OTP_RESEND_TOO_SOON
                );
            }
        }

        // Mark previous unused OTP as used
        otpCodeRepository
                .findFirstByUserIdAndPurposeAndUsedFalseOrderByCreatedAtDesc(
                        user.getId(),
                        OtpPurpose.FORGOT_PASSWORD
                )
                .ifPresent(otp -> {
                    otp.setUsed(true);
                    otpCodeRepository.save(otp);
                });

        // Generate OTP
        String code = String.format("%06d", RANDOM.nextInt(1_000_000));

        // Save OTP
        OtpCodeEntity otp = new OtpCodeEntity();
        otp.setUserId(user.getId());
        otp.setCodeHash(passwordEncoder.encode(code));
        otp.setPurpose(OtpPurpose.FORGOT_PASSWORD);
        otp.setExpiresAt(LocalDateTime.now().plusMinutes(EXPIRY_MINUTES));
        otp.setAttempts(0);
        otp.setUsed(false);

        otpCodeRepository.save(otp);

        // Send OTP email
        otpSender.send(user.getEmail(), code);
    }

    @Override
    @Transactional(noRollbackFor = BusinessException.class)
    public UserEntity verifyOtpForgotPassword(ResetPasswordRequest request) {

        UserEntity user = userRepository
                .findByEmailIgnoreCase(request.getEmail().trim())
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.OTP_INVALID
                ));

        OtpCodeEntity otp = otpCodeRepository
                .findFirstByUserIdAndPurposeAndUsedFalseOrderByCreatedAtDesc(
                        user.getId(),
                        OtpPurpose.FORGOT_PASSWORD
                )
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.OTP_INVALID
                ));

        // Check expiration
        if (LocalDateTime.now().isAfter(otp.getExpiresAt())) {
            otp.setUsed(true);
            otpCodeRepository.save(otp);

            throw new BusinessException(ErrorCode.OTP_EXPIRED);
        }

        // Check maximum attempts
        if (otp.getAttempts() >= MAX_ATTEMPTS) {
            otp.setUsed(true);
            otpCodeRepository.save(otp);

            throw new BusinessException(ErrorCode.OTP_TOO_MANY_ATTEMPTS);
        }

        // Verify OTP
        if (!passwordEncoder.matches(request.getOtp(), otp.getCodeHash())) {
            otp.setAttempts(otp.getAttempts() + 1);

            if (otp.getAttempts() >= MAX_ATTEMPTS) {
                otp.setUsed(true);
            }

            otpCodeRepository.save(otp);

            throw new BusinessException(ErrorCode.OTP_INVALID);
        }

        // Mark OTP as used
        otp.setUsed(true);
        otpCodeRepository.save(otp);

        return user;
    }

    @Override
    @Transactional
    public void sendLoginOtp(UserEntity user) {
        checkLoginCooldown(user);

        otpCodeRepository
                .findFirstByUserIdAndPurposeAndUsedFalseOrderByCreatedAtDesc(user.getId(), OtpPurpose.LOGIN)
                .ifPresent(old -> old.setUsed(true));

        String code = generateCode();

        OtpCodeEntity otp = new OtpCodeEntity();
        otp.setUserId(user.getId());
        otp.setCodeHash(passwordEncoder.encode(code));
        otp.setPurpose(OtpPurpose.LOGIN);
        otp.setExpiresAt(LocalDateTime.now().plusMinutes(EXPIRY_MINUTES));
        otpCodeRepository.save(otp);

        otpSender.send(user.getEmail(), code);
    }

    @Override
    @Transactional(noRollbackFor = BusinessException.class)
    public UserEntity verifyLoginOtp(VerifyOtpRequest request) {
        UserEntity user = userRepository.findByEmail(request.getEmail().trim().toLowerCase())
                .orElseThrow(() -> new BusinessException(ErrorCode.OTP_INVALID));

        if (!user.isEnabled()) {
            throw new BusinessException(ErrorCode.OTP_INVALID);
        }

        OtpCodeEntity otp = otpCodeRepository
                .findFirstByUserIdAndPurposeAndUsedFalseOrderByCreatedAtDesc(user.getId(), OtpPurpose.LOGIN)
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
        return user;
    }

    private void checkLoginCooldown(UserEntity user) {
        otpCodeRepository
                .findFirstByUserIdAndPurposeOrderByCreatedAtDesc(user.getId(), OtpPurpose.LOGIN)
                .filter(last -> last.getCreatedAt().isAfter(LocalDateTime.now().minusSeconds(RESEND_SECONDS)))
                .ifPresent(last -> { throw new BusinessException(ErrorCode.OTP_RESEND_TOO_SOON); });
    }

    private String generateCode() {
        return String.format("%06d", RANDOM.nextInt(1_000_000));
    }
}
package com.example.transaction_management.service.impl;

import com.example.transaction_management.common.errors.ErrorCode;
import com.example.transaction_management.common.exceptions.BusinessException;
import com.example.transaction_management.dto.request.*;
import com.example.transaction_management.dto.response.RegisterResponse;
import com.example.transaction_management.dto.response.UserResponse;
import com.example.transaction_management.entity.PendingRegistrationEntity;
import com.example.transaction_management.entity.UserEntity;
import com.example.transaction_management.mapper.UserMapper;
import com.example.transaction_management.repository.PendingRegistrationRepository;
import com.example.transaction_management.repository.UserRepository;
import com.example.transaction_management.service.AuthService;
import com.example.transaction_management.service.OtpService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PendingRegistrationRepository pendingRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final OtpService otpService;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;

    private final SecurityContextRepository securityContextRepository =
            new HttpSessionSecurityContextRepository();


    @Override
    @Transactional
    public RegisterResponse registerAccount(RegisterRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        String username = request.getUsername().trim();

        if (userRepository.existsByEmail(email)) {
            throw new BusinessException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }
        if (userRepository.existsByUsername(username)) {
            throw new BusinessException(ErrorCode.USERNAME_ALREADY_EXISTS);
        }

        // registering the same email again replaces the pending data and sends a new code
        PendingRegistrationEntity pending = pendingRepository.findByEmail(email)
                .orElseGet(PendingRegistrationEntity::new);

        pending.setEmail(email);
        pending.setUsername(username);
        pending.setPasswordHash(passwordEncoder.encode(request.getPassword()));

        otpService.sendRegisterOtp(pending);

        return new RegisterResponse(email, true,
                "Verification code sent. Your account is created after you verify.");
    }

    @Override
    @Transactional
    public String login(LoginRequest request) {
        String email = request.getEmail().trim().toLowerCase();

        // throws BadCredentialsException (wrong password) or DisabledException
        authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(email, request.getPassword()));

        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_CREDENTIALS));

        otpService.sendLoginOtp(user);
        return ("Verification code sent to your email.");
    }

    @Override
    public UserResponse verifyLogin(VerifyOtpRequest request,
                                    HttpServletRequest httpRequest,
                                    HttpServletResponse httpResponse) {

        UserEntity user = otpService.verifyLoginOtp(request);

        UserDetails details = userDetailsService.loadUserByUsername(user.getEmail());
        Authentication authentication = UsernamePasswordAuthenticationToken
                .authenticated(details, null, details.getAuthorities());

        if (httpRequest.getSession(false) != null) {
            httpRequest.changeSessionId();                  // protects against session fixation
        }

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        securityContextRepository.saveContext(context, httpRequest, httpResponse);

        return userMapper.toResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHENTICATED));
        return userMapper.toResponse(user);
    }

    @Override
    public String forgotPassword(ForgetPasswordRequest request) {

        String email = request.getEmail().trim().toLowerCase();

        UserEntity user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        otpService.sendForgotPasswordOtp(user);

        // Generic response prevents email enumeration
        return "If the account exists, a password reset OTP has been sent.";
    }

    @Override
    @Transactional
    public String resetPassword(ResetPasswordRequest request) {

        // Verify OTP and retrieve user
        UserEntity user = otpService.verifyOtpForgotPassword(request);

        // Encode and update new password
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));

        userRepository.save(user);

        return "Password reset successfully";
    }
}
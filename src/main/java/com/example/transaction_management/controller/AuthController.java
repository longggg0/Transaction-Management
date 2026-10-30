package com.example.transaction_management.controller;

import com.example.transaction_management.dto.request.RegisterRequest;
import com.example.transaction_management.dto.request.ResendOtpRequest;
import com.example.transaction_management.dto.request.VerifyOtpRequest;
import com.example.transaction_management.dto.response.MessageResponse;
import com.example.transaction_management.dto.response.UserResponse;
import com.example.transaction_management.service.AuthService;
import com.example.transaction_management.service.OtpSenderService;
import com.example.transaction_management.service.OtpService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final OtpService otpService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.registerAccount(request);
    }

    @PostMapping("/verify-otp")
    public MessageResponse verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
        return otpService.verifyRegisterOtp(request);
    }

    @PostMapping("/resend-otp")
    public MessageResponse resendOtp(@Valid @RequestBody ResendOtpRequest request) {
        return otpService.resendRegisterOtp(request);
    }

    @GetMapping("/me")
    public UserResponse me() {
        return authService.getCurrentUser();
    }
}

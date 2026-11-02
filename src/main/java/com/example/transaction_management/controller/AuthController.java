package com.example.transaction_management.controller;

import com.example.transaction_management.common.response.ApiResponse;
import com.example.transaction_management.dto.request.*;
import com.example.transaction_management.dto.response.RegisterResponse;
import com.example.transaction_management.dto.response.UserResponse;
import com.example.transaction_management.service.AuthService;
import com.example.transaction_management.service.OtpService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
    public ApiResponse<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
        RegisterResponse response = authService.registerAccount(request);
        return ApiResponse.success("Register successfully", response);
    }

    @PostMapping("/verify-otp")
    public ApiResponse<Void> verifyRegisterOtp(@Valid @RequestBody VerifyOtpRequest request) {
        String message = otpService.verifyRegisterOtp(request);
        return ApiResponse.success(message, null);
    }

    @PostMapping("/resend-otp")
    public ApiResponse<Void> resendRegisterOtp(@Valid @RequestBody ResendOtpRequest request) {
        String message = otpService.resendRegisterOtp(request);
        return ApiResponse.success(message, null);
    }

    @PostMapping("/login")
    public ApiResponse<Void> login(@Valid @RequestBody LoginRequest request) {
        String message = authService.login(request);
        return ApiResponse.success(message, null);
    }

    @PostMapping("/login/verify")
    public ApiResponse<UserResponse> verifyLogin(@Valid @RequestBody VerifyOtpRequest request,
                                    HttpServletRequest httpRequest,
                                    HttpServletResponse httpResponse) {
        UserResponse response = authService.verifyLogin(request, httpRequest, httpResponse);
        return ApiResponse.success("Verified login successfully",response);
    }

    @PostMapping("/forgot-password")
    public ApiResponse<Void> forgotPassword(
            @Valid @RequestBody ForgetPasswordRequest request) {

        String message = authService.forgotPassword(request);

        return ApiResponse.success(message, null);
    }

    @PostMapping("/reset-password")
    public ApiResponse<Void> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request) {

        String message = authService.resetPassword(request);

        return ApiResponse.success(message, null);
    }

    @GetMapping("/me")
    public UserResponse me() {
        return authService.getCurrentUser();
    }
}
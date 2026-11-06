package com.example.transaction_management.controller;

import com.example.transaction_management.common.response.ApiResponse;
import com.example.transaction_management.dto.request.*;
import com.example.transaction_management.dto.response.MessageResponse;
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
    public ApiResponse<MessageResponse> login(@Valid @RequestBody LoginRequest request) {
        MessageResponse response = authService.login(request);
        return ApiResponse.success("Get your OTP and verify your login account",response);
    }

    @PostMapping("/login/verify")
    public ApiResponse<UserResponse> verifyLogin(@Valid @RequestBody VerifyOtpRequest request,
                                    HttpServletRequest httpRequest,
                                    HttpServletResponse httpResponse) {
        UserResponse response = authService.verifyLogin(request, httpRequest, httpResponse);
        return ApiResponse.success("Verified login successfully",response);
    }

    @PostMapping("/forgot-password")
    public ApiResponse<MessageResponse> forgotPassword(
            @Valid @RequestBody ForgetPasswordRequest request) {

        MessageResponse response = authService.forgotPassword(request);

        return ApiResponse.success("Get OTP for verify your reset password  ",response);
    }

    @PostMapping("/reset-password")
    public ApiResponse<MessageResponse> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request) {

        MessageResponse response = authService.resetPassword(request);

        return ApiResponse.success("Reset password successfully",response);
    }


    @PostMapping("/login/resend-otp")
    public ApiResponse<MessageResponse> resendLoginOtp(
            @Valid @RequestBody LoginRequest request) {

        MessageResponse response = authService.resendLoginOtp(request);

        return ApiResponse.success("Resend OTP for login successfully",response);
    }

    @PostMapping("/forgot-password/resend-otp")
    public ApiResponse<MessageResponse> resendForgotPasswordOtp(@Valid @RequestBody ForgetPasswordRequest request){

        MessageResponse response = authService.resendForgotPasswordOtp(request);

        return ApiResponse.success("Resend OTP for forgot password successfully",response);

    }

    @GetMapping("/me")
    public UserResponse me() {
        return authService.getCurrentUser();
    }
}
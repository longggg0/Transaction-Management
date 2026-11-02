package com.example.transaction_management.service;

import com.example.transaction_management.dto.request.*;
import com.example.transaction_management.dto.response.RegisterResponse;
import com.example.transaction_management.dto.response.UserResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface AuthService {

    RegisterResponse registerAccount(RegisterRequest request);

    String login(LoginRequest request);

    UserResponse verifyLogin(VerifyOtpRequest request,
                             HttpServletRequest httpRequest,
                             HttpServletResponse httpResponse);

    UserResponse getCurrentUser();

    String forgotPassword(ForgetPasswordRequest request);

    String resetPassword(ResetPasswordRequest request);

}

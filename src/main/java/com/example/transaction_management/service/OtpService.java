package com.example.transaction_management.service;

import com.example.transaction_management.dto.request.ResendOtpRequest;
import com.example.transaction_management.dto.request.ResetPasswordRequest;
import com.example.transaction_management.dto.request.VerifyOtpRequest;
import com.example.transaction_management.entity.PendingRegistrationEntity;
import com.example.transaction_management.entity.UserEntity;

public interface OtpService {

    void sendRegisterOtp(PendingRegistrationEntity pending);

    String verifyRegisterOtp(VerifyOtpRequest request);

    String resendRegisterOtp(ResendOtpRequest request);

    void sendLoginOtp(UserEntity user);

    UserEntity verifyLoginOtp(VerifyOtpRequest request);

    void sendForgotPasswordOtp(UserEntity user);

    UserEntity verifyOtpForgotPassword(ResetPasswordRequest request );

//    void resendForgotPasswordOtp();
}

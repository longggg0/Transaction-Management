package com.example.transaction_management.service;

import com.example.transaction_management.dto.request.ResendOtpRequest;
import com.example.transaction_management.dto.request.VerifyOtpRequest;
import com.example.transaction_management.dto.response.MessageResponse;
import com.example.transaction_management.entity.UserEntity;

public interface OtpService {

    void sendRegisterOtp(UserEntity user);

    MessageResponse verifyRegisterOtp(VerifyOtpRequest request);

    MessageResponse resendRegisterOtp(ResendOtpRequest request);
}

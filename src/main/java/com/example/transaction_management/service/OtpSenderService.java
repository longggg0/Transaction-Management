package com.example.transaction_management.service;

public interface OtpSenderService {
    void send(String email, String code);
}

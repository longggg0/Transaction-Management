package com.example.transaction_management.service;

import com.example.transaction_management.dto.request.RegisterRequest;
import com.example.transaction_management.dto.response.UserResponse;

public interface AuthService {

    UserResponse registerAccount(RegisterRequest request);

    UserResponse getCurrentUser();

}

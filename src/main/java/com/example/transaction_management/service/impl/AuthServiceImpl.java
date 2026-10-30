package com.example.transaction_management.service.impl;

import com.example.transaction_management.common.enums.Role;
import com.example.transaction_management.common.errors.ErrorCode;
import com.example.transaction_management.common.exceptions.BusinessException;
import com.example.transaction_management.dto.request.RegisterRequest;
import com.example.transaction_management.dto.response.UserResponse;
import com.example.transaction_management.entity.UserEntity;
import com.example.transaction_management.mapper.UserMapper;
import com.example.transaction_management.repository.UserRepository;
import com.example.transaction_management.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public UserResponse registerAccount(RegisterRequest request) {

        String email = request.getEmail().trim().toLowerCase();
        String username = request.getUsername().trim();

        if (userRepository.existsByEmail(email)) {
            throw new BusinessException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }
        if (userRepository.existsByUsername(username)) {
            throw new BusinessException(ErrorCode.USERNAME_ALREADY_EXISTS);
        }

        UserEntity user = userMapper.toEntity(request);
        user.setEmail(email);
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.USER);

        return userMapper.toResponse(userRepository.save(user));
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHENTICATED));
        return userMapper.toResponse(user);
    }
}

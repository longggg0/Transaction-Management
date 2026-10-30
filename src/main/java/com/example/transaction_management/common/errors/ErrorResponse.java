package com.example.transaction_management.common.errors;

import java.time.LocalDateTime;
import java.util.Map;

public record ErrorResponse(
        LocalDateTime timestamp,
        int status,
        String code,
        String message,
        String path,
        Map<String, String> fieldErrors   // null unless validation failed
) {}

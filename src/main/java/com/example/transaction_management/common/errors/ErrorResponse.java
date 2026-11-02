package com.example.transaction_management.common.errors;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Builder
public class ErrorResponse {

    private boolean success;
    private String code;
    private String message;
    private Object data;
    private LocalDateTime timestamp;
    private String path;
    private Map<String, String> errors;

    public static ErrorResponse of(
            ErrorCode errorCode,
            String message,
            String path,
            Map<String, String> errors
    ) {
        return ErrorResponse.builder()
                .success(false)
                .code(errorCode.getCode())
                .message(message)
                .data(null)
                .timestamp(LocalDateTime.now())
                .path(path)
                .errors(errors)
                .build();
    }
}
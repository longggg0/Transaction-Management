package com.example.transaction_management.common.exceptions;

import com.example.transaction_management.common.errors.ErrorCode;
import com.example.transaction_management.common.errors.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(TransactionManagementException.class)
    public ResponseEntity<ErrorResponse> handleApp(
            TransactionManagementException ex,
            HttpServletRequest req
    ) {
        return build(
                ex.getErrorCode(),
                ex.getMessage(),
                req,
                null
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(
            MethodArgumentNotValidException ex,
            HttpServletRequest req
    ) {
        Map<String, String> fields = new LinkedHashMap<>();

        ex.getBindingResult().getFieldErrors()
                .forEach(error ->
                        fields.put(error.getField(), error.getDefaultMessage())
                );

        return build(
                ErrorCode.VALIDATION_FAILED,
                ErrorCode.VALIDATION_FAILED.getMessage(),
                req,
                fields
        );
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(
            HttpServletRequest req
    ) {
        return build(
                ErrorCode.INVALID_CREDENTIALS,
                ErrorCode.INVALID_CREDENTIALS.getMessage(),
                req,
                null
        );
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrity(
            DataIntegrityViolationException ex,
            HttpServletRequest req
    ) {
        log.warn(
                "Data integrity violation: {}",
                ex.getMostSpecificCause().getMessage()
        );

        return build(
                ErrorCode.DATA_CONFLICT,
                ErrorCode.DATA_CONFLICT.getMessage(),
                req,
                null
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(
            Exception ex,
            HttpServletRequest req
    ) {
        log.error("Unexpected error on {}", req.getRequestURI(), ex);

        return build(
                ErrorCode.INTERNAL_ERROR,
                ErrorCode.INTERNAL_ERROR.getMessage(),
                req,
                null
        );
    }

    private ResponseEntity<ErrorResponse> build(
            ErrorCode errorCode,
            String message,
            HttpServletRequest req,
            Map<String, String> fieldErrors
    ) {
        ErrorResponse body = ErrorResponse.of(
                errorCode,
                message,
                req.getRequestURI(),
                fieldErrors
        );

        return ResponseEntity
                .status(errorCode.getStatus())
                .body(body);
    }
}
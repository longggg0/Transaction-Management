package com.example.transaction_management.common.exceptions;

import com.example.transaction_management.common.errors.ErrorCode;
import lombok.Getter;

@Getter
public class TransactionManagementException extends RuntimeException {

    private final ErrorCode errorCode;

    public TransactionManagementException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public TransactionManagementException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public TransactionManagementException(ErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
}

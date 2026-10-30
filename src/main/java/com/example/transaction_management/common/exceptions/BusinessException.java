package com.example.transaction_management.common.exceptions;

import com.example.transaction_management.common.errors.ErrorCode;

public class BusinessException extends TransactionManagementException{

    public BusinessException(ErrorCode errorCode){
        super(errorCode);
    }

    public BusinessException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    public BusinessException(ErrorCode errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }

}

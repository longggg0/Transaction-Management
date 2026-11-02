package com.example.transaction_management.common.errors;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    // Auth
    EMAIL_ALREADY_EXISTS("AUTH_001", HttpStatus.CONFLICT, "Email already registered"),
    USERNAME_ALREADY_EXISTS("AUTH_002", HttpStatus.CONFLICT, "Username already taken"),
    USER_NOT_FOUND("AUTH_05",HttpStatus.NOT_FOUND,"User not found"),
    INVALID_CREDENTIALS("AUTH_003", HttpStatus.UNAUTHORIZED, "Invalid email or password"),
    UNAUTHENTICATED("AUTH_004", HttpStatus.UNAUTHORIZED, "Please log in first"),

    //OTP
    OTP_INVALID("OTP_01",HttpStatus.BAD_REQUEST, "Invalid or expired code"),
    OTP_EXPIRED("OTP_02",HttpStatus.BAD_REQUEST, "Code has expired, request a new one"),
    OTP_TOO_MANY_ATTEMPTS("OTP_03",HttpStatus.TOO_MANY_REQUESTS, "Too many attempts, request a new code"),
    OTP_RESEND_TOO_SOON("OTP_04",HttpStatus.TOO_MANY_REQUESTS, "Please wait before requesting another code"),
    EMAIL_SEND_FAILED("OTP_05",HttpStatus.SERVICE_UNAVAILABLE, "Could not send the email, please try again"),

    // Category
    CATEGORY_NOT_FOUND("CAT_001", HttpStatus.NOT_FOUND, "Category not found"),
    CATEGORY_ALREADY_EXISTS("CAT_002", HttpStatus.CONFLICT, "Category already exists"),

    // Transaction
    TRANSACTION_NOT_FOUND("TXN_001", HttpStatus.NOT_FOUND, "Transaction not found"),
    INVALID_AMOUNT("TXN_002", HttpStatus.BAD_REQUEST, "Amount must be greater than zero"),

    // General
    VALIDATION_FAILED("GEN_001", HttpStatus.BAD_REQUEST, "Validation failed"),
    DATA_CONFLICT("GEN_002", HttpStatus.CONFLICT, "Data conflicts with existing records"),
    INTERNAL_ERROR("GEN_999", HttpStatus.INTERNAL_SERVER_ERROR, "Something went wrong");

    private final String code;
    private final HttpStatus status;
    private final String message;

    ErrorCode(String code, HttpStatus status, String message) {
        this.code = code;
        this.status = status;
        this.message = message;
    }
}

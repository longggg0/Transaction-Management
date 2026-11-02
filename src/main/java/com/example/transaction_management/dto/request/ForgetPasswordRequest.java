package com.example.transaction_management.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ForgetPasswordRequest {

    @NotBlank(message = "Email is required")
    @Email
    private String email;

}

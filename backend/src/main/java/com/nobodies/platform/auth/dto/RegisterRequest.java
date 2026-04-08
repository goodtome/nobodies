package com.nobodies.platform.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {
    @Email(message = "Email format is invalid")
    @NotBlank(message = "Email is required")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 64, message = "Password length must be between 8 and 64")
    private String password;

    @NotBlank(message = "Confirm password is required")
    @Size(min = 8, max = 64, message = "Confirm password length must be between 8 and 64")
    private String confirmPassword;
}

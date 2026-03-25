package com.meradeya.app.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Login request payload")
public record LoginRequest(
    @Schema(description = "User's email address", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank @Email
    String email,

    @Schema(description = "User's password", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    String password
) {

}


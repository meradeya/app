package com.meradeya.app.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Email verification request payload")
public record VerifyEmailRequest(
    @Schema(description = "Single-use email verification token", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    String token
) {

}


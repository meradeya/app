package com.meradeya.app.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Password reset request payload")
public record RequestPasswordResetRequest(
    @Schema(description = "Email address of the account", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank @Email
    String email
) {

}


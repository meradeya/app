package com.meradeya.app.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Password reset confirmation payload")
public record ConfirmPasswordResetRequest(
    @Schema(description = "Single-use password reset token", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    String token,

    @Schema(description = "New password (min 8 characters)", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank @Size(min = 8)
    String newPassword
) {

}


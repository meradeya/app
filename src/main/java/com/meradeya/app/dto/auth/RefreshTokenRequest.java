package com.meradeya.app.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Refresh token payload")
public record RefreshTokenRequest(
    @Schema(description = "The refresh token to use or revoke", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    String refreshToken
) {

}


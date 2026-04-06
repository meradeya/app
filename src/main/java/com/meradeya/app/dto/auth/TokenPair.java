package com.meradeya.app.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "JWT access + refresh token pair")
public record TokenPair(
    @Schema(description = "Short-lived JWT access token") String accessToken,
    @Schema(description = "Long-lived refresh token") String refreshToken,
    @Schema(description = "Access token lifetime in seconds", example = "900") long expiresIn
) {

}


package com.meradeya.app.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

@Schema(description = "Successful registration response — token pair + user info")
public record RegisterResponse(
    @Schema(description = "Newly created user ID") UUID userId,
    @Schema(description = "Registered email address") String email,
    @Schema(description = "Short-lived JWT access token") String accessToken,
    @Schema(description = "Long-lived refresh token") String refreshToken,
    @Schema(description = "Access token lifetime in seconds", example = "900") long expiresIn
) {

}


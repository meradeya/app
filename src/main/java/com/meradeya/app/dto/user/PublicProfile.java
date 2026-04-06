package com.meradeya.app.dto.user;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

@Schema(description = "Public-facing user profile (no private fields)")
@JsonInclude(JsonInclude.Include.NON_NULL)
public record PublicProfile(
    @Schema(description = "User's unique ID") UUID userId,
    @Schema(description = "Display name") String displayName,
    @Schema(description = "Avatar URL") String avatarUrl,
    @Schema(description = "Location") String location,
    @Schema(description = "Bio") String bio
) {

}


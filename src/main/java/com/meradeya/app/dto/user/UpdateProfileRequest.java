package com.meradeya.app.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Partial update payload for the authenticated user's profile")
public record UpdateProfileRequest(
    @Schema(description = "New display name")
    @Size(max = 100)
    String displayName,

    @Schema(description = "New avatar URL")
    String avatarUrl,

    @Schema(description = "New location")
    @Size(max = 200)
    String location,

    @Schema(description = "New bio")
    String bio,

    @Schema(description = "Current version for optimistic locking (from last GET)")
    @NotBlank
    Long version
) {

}


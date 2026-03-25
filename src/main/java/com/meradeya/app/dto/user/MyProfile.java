package com.meradeya.app.dto.user;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.OffsetDateTime;
import java.util.UUID;

@Schema(description = "Authenticated user's full profile including private fields")
@JsonInclude(JsonInclude.Include.NON_NULL)
public record MyProfile(
    @Schema(description = "User's unique ID") UUID userId,
    @Schema(description = "Email address") String email,
    @Schema(description = "Whether the email has been verified") Boolean emailVerified,
    @Schema(description = "Account status", allowableValues = {"ACTIVE", "SUSPENDED",
        "DELETED"}) String status,
    @Schema(description = "Display name") String displayName,
    @Schema(description = "Avatar URL") String avatarUrl,
    @Schema(description = "Location") String location,
    @Schema(description = "Bio") String bio,
    @Schema(description = "Last updated timestamp") OffsetDateTime updatedAt,
    @Schema(description = "Optimistic lock version") Long version
) {

}


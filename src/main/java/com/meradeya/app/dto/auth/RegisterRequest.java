package com.meradeya.app.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Registration request payload")
public record RegisterRequest(
    @Schema(description = "User's email address", example = "ada@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank @Email
    String email,

    @Schema(description = "Password (min 8 characters)", example = "S3cur3Pass!", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank @Size(min = 8)
    String password,

    @Schema(description = "Display name shown on the profile", example = "Ada Lovelace", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank @Size(max = 100)
    String displayName
) {

}


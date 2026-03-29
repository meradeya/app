package com.meradeya.app.controller.api;

import com.meradeya.app.dto.auth.ConfirmPasswordResetRequest;
import com.meradeya.app.dto.auth.LoginRequest;
import com.meradeya.app.dto.auth.RefreshTokenRequest;
import com.meradeya.app.dto.auth.RegisterRequest;
import com.meradeya.app.dto.auth.RegisterResponse;
import com.meradeya.app.dto.auth.RequestPasswordResetRequest;
import com.meradeya.app.dto.auth.TokenPair;
import com.meradeya.app.dto.auth.VerifyEmailRequest;
import com.meradeya.app.dto.common.ExceptionDetail;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "Auth", description = "Authentication and token management")
@RequestMapping(value = "/v{version}", version = "1.0")
public interface AuthControllerApi {

  @Operation(
      operationId = "registerUser",
      summary = "Register a new user",
      description = """
          Creates a new user account and a matching profile.
          Sends an email-verification message automatically.
          The account starts with emailVerified: false; publishing a listing requires a verified email.
          """,
      responses = {
          @ApiResponse(responseCode = "201", description = "User created; token pair issued; verification email dispatched",
              content = @Content(mediaType = "application/json", schema = @Schema(implementation = RegisterResponse.class))),
          @ApiResponse(responseCode = "400", description = "Validation error",
              content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionDetail.class))),
          @ApiResponse(responseCode = "409", description = "Email already registered",
              content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionDetail.class),
                  examples = @ExampleObject(value = """
                      {"type":"about:blank","title":"Email Already Exists","status":409,"detail":"An account with this email already exists."}
                      """)))
      }
  )
  @PostMapping("/auth/register")
  ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest request);

  @Operation(
      operationId = "login",
      summary = "Authenticate and obtain tokens",
      description = """
          Validates email + password. Returns a short-lived JWT access token and
          a long-lived refresh token. Suspended accounts receive 403.
          """,
      responses = {
          @ApiResponse(responseCode = "200", description = "Authenticated",
              content = @Content(mediaType = "application/json", schema = @Schema(implementation = TokenPair.class))),
          @ApiResponse(responseCode = "401", description = "Invalid credentials"),
          @ApiResponse(responseCode = "403", description = "Account suspended",
              content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionDetail.class),
                  examples = @ExampleObject(value = """
                      {"type":"about:blank","title":"Account Suspended","status":403,"detail":"Your account has been suspended."}
                      """)))
      }
  )
  @PostMapping("/auth/login")
  ResponseEntity<TokenPair> login(@Valid @RequestBody LoginRequest request);

  @Operation(
      operationId = "refreshToken",
      summary = "Rotate token pair",
      description = """
          Exchanges a valid refresh token for a new access + refresh token pair.
          The submitted refresh token is immediately revoked (rotation).
          """,
      responses = {
          @ApiResponse(responseCode = "200", description = "New token pair issued",
              content = @Content(mediaType = "application/json", schema = @Schema(implementation = TokenPair.class))),
          @ApiResponse(responseCode = "401", description = "Refresh token invalid, expired, or already revoked")
      }
  )
  @PostMapping("/auth/refresh")
  ResponseEntity<TokenPair> refresh(@Valid @RequestBody RefreshTokenRequest request);

  @Operation(
      operationId = "logout",
      summary = "Revoke refresh token",
      description = """
          Marks the supplied refresh token as revoked. Idempotent — revoking an already-revoked
          or unknown token still returns 204. No JWT required: the refresh token is itself the
          credential being invalidated, and this endpoint must remain reachable regardless of
          access-token state (e.g. when the JWT has already expired).
          """,
      responses = {
          @ApiResponse(responseCode = "204", description = "Token revoked (or was already revoked)")
      }
  )
  @PostMapping("/auth/logout")
  ResponseEntity<Void> logout(@Valid @RequestBody RefreshTokenRequest request);

  @Operation(
      operationId = "verifyEmail",
      summary = "Confirm email address",
      description = """
          Consumes a single-use EMAIL_VERIFY token. Sets email_verified = true.
          Token is valid for 24 hours.
          """,
      responses = {
          @ApiResponse(responseCode = "204", description = "Email verified"),
          @ApiResponse(responseCode = "400", description = "Token invalid, expired, or already used",
              content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionDetail.class),
                  examples = @ExampleObject(value = """
                      {"type":"about:blank","title":"Invalid Token","status":400,"detail":"The verification token is invalid or has expired."}
                      """)))
      }
  )
  @PostMapping("/auth/verify-email")
  ResponseEntity<Void> verifyEmail(@Valid @RequestBody VerifyEmailRequest request);

  @Operation(
      operationId = "requestPasswordReset",
      summary = "Request a password-reset email",
      description = """
          Generates a PASSWORD_RESET auth_token and emails it to the user.
          Always responds 204 to prevent email enumeration.
          """,
      responses = {
          @ApiResponse(responseCode = "204", description = "Reset email dispatched (or silently ignored if unknown)")
      }
  )
  @PostMapping("/auth/password-reset/request")
  ResponseEntity<Void> requestPasswordReset(
      @Valid @RequestBody RequestPasswordResetRequest request);

  @Operation(
      operationId = "confirmPasswordReset",
      summary = "Set a new password using reset token",
      description = """
          Consumes a single-use PASSWORD_RESET token and replaces the password hash.
          All existing refresh tokens for the user are revoked on success.
          """,
      responses = {
          @ApiResponse(responseCode = "204", description = "Password updated; all sessions invalidated"),
          @ApiResponse(responseCode = "400", description = "Token invalid, expired, or already used")
      }
  )
  @PostMapping("/auth/password-reset/confirm")
  ResponseEntity<Void> confirmPasswordReset(
      @Valid @RequestBody ConfirmPasswordResetRequest request);
}


package com.meradeya.app.api;

import com.meradeya.app.generated.api.AuthApiDelegate;
import com.meradeya.app.generated.api.model.ConfirmPasswordResetRequest;
import com.meradeya.app.generated.api.model.LoginRequest;
import com.meradeya.app.generated.api.model.RefreshTokenRequest;
import com.meradeya.app.generated.api.model.RegisterResponse;
import com.meradeya.app.generated.api.model.RegisterUserRequest;
import com.meradeya.app.generated.api.model.RequestPasswordResetRequest;
import com.meradeya.app.generated.api.model.TokenPair;
import com.meradeya.app.generated.api.model.VerifyEmailRequest;
import com.meradeya.app.service.face.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

/**
 * Implementation of {@link AuthApiDelegate}. Thin HTTP adapter — all business logic lives in
 * {@link AuthService}.
 */
@Service
@RequiredArgsConstructor
public class AuthApiDelegateImpl implements AuthApiDelegate {

  private final AuthService authService;

  /**
   * POST /auth/register Creates a new user account, dispatches a verification email,
   * and returns a token pair so the user is immediately authenticated.
   */
  @Override
  public ResponseEntity<RegisterResponse> registerUser(RegisterUserRequest req) {
    RegisterResponse body = authService.registerUser(req.getEmail(), req.getPassword(), req.getDisplayName());
    return ResponseEntity.status(HttpStatus.CREATED).body(body);
  }

  /**
   * POST /auth/login Validates credentials and returns a JWT access token + refresh token pair.
   */
  @Override
  public ResponseEntity<TokenPair> login(LoginRequest req) {
    TokenPair pair = authService.login(req.getEmail(), req.getPassword());
    return ResponseEntity.ok(pair);
  }

  /**
   * POST /auth/logout Revokes the supplied refresh token. Idempotent.
   */
  @Override
  public ResponseEntity<Void> logout(RefreshTokenRequest req) {
    authService.logout(req.getRefreshToken());
    return ResponseEntity.noContent().build();
  }

  /**
   * POST /auth/refresh Rotates the token pair — consumes the old refresh token and issues a new
   * pair.
   */
  @Override
  public ResponseEntity<TokenPair> refreshToken(RefreshTokenRequest req) {
    TokenPair pair = authService.refresh(req.getRefreshToken());
    return ResponseEntity.ok(pair);
  }

  /**
   * POST /auth/verify-email Consumes a single-use EMAIL_VERIFY token and marks the email as
   * verified.
   */
  @Override
  public ResponseEntity<Void> verifyEmail(VerifyEmailRequest req) {
    authService.verifyEmail(req.getToken());
    return ResponseEntity.noContent().build();
  }

  /**
   * POST /auth/password-reset/request Generates a PASSWORD_RESET token and emails it (always 204 to
   * prevent enumeration).
   */
  @Override
  public ResponseEntity<Void> requestPasswordReset(RequestPasswordResetRequest req) {
    authService.requestPasswordReset(req.getEmail());
    return ResponseEntity.noContent().build();
  }

  /**
   * POST /auth/password-reset/confirm Consumes a PASSWORD_RESET token and replaces the password
   * hash.
   */
  @Override
  public ResponseEntity<Void> confirmPasswordReset(ConfirmPasswordResetRequest req) {
    authService.confirmPasswordReset(req.getToken(), req.getNewPassword());
    return ResponseEntity.noContent().build();
  }

}

package com.meradeya.app.service.face;

import com.meradeya.app.domain.entity.AuthToken;
import com.meradeya.app.domain.entity.User;
import com.meradeya.app.dto.auth.TokenPair;

/**
 * Owns all token lifecycle concerns:
 * <ul>
 *   <li>JWT access-token creation and signing</li>
 *   <li>Opaque refresh-token issuance and rotation</li>
 *   <li>One-time {@code AuthToken} creation and consumption (email verify, password reset)</li>
 *   <li>Cryptographic helpers (opaque token generation, SHA-256 hashing)</li>
 * </ul>
 *
 * <p>All mutating operations run within the caller's transaction
 * (Spring {@code REQUIRED} propagation).
 */
public interface TokenService {

  /**
   * Creates a signed JWT access token and a new {@link com.meradeya.app.domain.entity.RefreshToken},
   * binds the refresh token to {@code user}, persists it, and returns the pair.
   */
  TokenPair createTokenPair(User user);

  /**
   * Revokes the refresh token matching {@code rawRefreshToken}.
   * Idempotent — no error if the token is already revoked or does not exist.
   */
  void revokeRefreshToken(String rawRefreshToken);

  /**
   * Validates the given raw refresh token, revokes it, and issues a fresh {@link TokenPair}
   * (token rotation).
   *
   * @throws com.meradeya.app.exception.InvalidTokenException if the token is invalid or expired
   */
  TokenPair rotateRefreshToken(String rawRefreshToken);

  /**
   * Creates an {@code EMAIL_VERIFY} {@link AuthToken} entity ready to be attached to a user.
   * The raw token is embedded in the entity only as a hash; callers are responsible for
   * delivering the raw token to the user (e.g. via a domain event).
   */
  AuthToken createEmailVerifyToken();

  /**
   * Creates a {@code PASSWORD_RESET} {@link AuthToken} entity ready to be attached to a user.
   */
  AuthToken createPasswordResetToken();

  /**
   * Validates the raw email-verification token, marks it as used, and returns the entity.
   *
   * @throws com.meradeya.app.exception.InvalidTokenException if the token is invalid, expired,
   *                                                           or already used
   */
  AuthToken consumeEmailVerifyToken(String rawToken);

  /**
   * Validates the raw password-reset token, marks it as used, and returns the entity.
   *
   * @throws com.meradeya.app.exception.InvalidTokenException if the token is invalid, expired,
   *                                                           or already used
   */
  AuthToken consumePasswordResetToken(String rawToken);

  /**
   * Revokes all active refresh tokens belonging to {@code user}.
   * Used after a successful password reset.
   */
  void revokeAllRefreshTokens(User user);
}


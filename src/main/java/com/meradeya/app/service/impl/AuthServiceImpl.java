package com.meradeya.app.service.impl;

import at.favre.lib.crypto.bcrypt.BCrypt;
import com.meradeya.app.domain.entity.AuthToken;
import com.meradeya.app.domain.entity.AuthTokenType;
import com.meradeya.app.domain.entity.RefreshToken;
import com.meradeya.app.domain.entity.User;
import com.meradeya.app.domain.entity.UserProfile;
import com.meradeya.app.domain.entity.UserStatus;
import com.meradeya.app.domain.repository.AuthTokenRepository;
import com.meradeya.app.domain.repository.RefreshTokenRepository;
import com.meradeya.app.domain.repository.UserRepository;
import com.meradeya.app.exception.AccountSuspendedException;
import com.meradeya.app.exception.EmailAlreadyExistsException;
import com.meradeya.app.exception.InvalidCredentialsException;
import com.meradeya.app.exception.InvalidTokenException;
import com.meradeya.app.generated.api.model.RegisterResponse;
import com.meradeya.app.generated.api.model.TokenPair;
import com.meradeya.app.prop.AuthProperties;
import com.meradeya.app.service.face.AuthService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.HexFormat;
import javax.crypto.SecretKey;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

  private final UserRepository userRepository;
  private final AuthTokenRepository authTokenRepository;
  private final RefreshTokenRepository refreshTokenRepository;
  private final AuthProperties authProperties;

  private final SecureRandom secureRandom = new SecureRandom();

  @Override
  public RegisterResponse registerUser(String email, String rawPassword, String displayName) {
    String normalizedEmail = email.toLowerCase();
    if (userRepository.existsByEmail(normalizedEmail)) {
      throw new EmailAlreadyExistsException(normalizedEmail);
    }

    String passwordHash = BCrypt.withDefaults().hashToString(12, rawPassword.toCharArray());
    User user = new User(normalizedEmail, passwordHash);
    UserProfile profile = new UserProfile(user, displayName);
    user.setProfile(profile);
    user.addAuthToken(createEmailVerifyToken());
    userRepository.save(user);

    log.info("Registered new user id={} email={}", user.getId(), normalizedEmail);
    // TODO: publish domain event / send verification email via async event

    TokenPair tokens = issueTokenPair(user);
    return new RegisterResponse()
        .userId(user.getId())
        .email(user.getEmail())
        .accessToken(tokens.getAccessToken())
        .refreshToken(tokens.getRefreshToken())
        .expiresIn(tokens.getExpiresIn());
  }

  @Override
  public TokenPair login(String email, String rawPassword) {
    User user = userRepository.findByEmail(email.toLowerCase())
        .orElseThrow(InvalidCredentialsException::new);

    if (!BCrypt.verifyer().verify(rawPassword.toCharArray(), user.getPasswordHash()).verified) {
      throw new InvalidCredentialsException();
    }

    if (user.getStatus() == UserStatus.SUSPENDED) {
      throw new AccountSuspendedException();
    }

    return issueTokenPair(user);
  }

  @Override
  public void logout(String rawRefreshToken) {
    String hash = sha256Hex(rawRefreshToken);
    refreshTokenRepository.findActiveByHash(hash, Instant.now())
        .ifPresent(rt -> rt.setRevoked(true));
    // Idempotent – no error if not found
  }

  @Override
  public TokenPair refresh(String rawRefreshToken) {
    String hash = sha256Hex(rawRefreshToken);
    RefreshToken rt = refreshTokenRepository.findActiveByHash(hash, Instant.now())
        .orElseThrow(InvalidTokenException::new);

    rt.setRevoked(true);

    return issueTokenPair(rt.getUser());
  }

  @Override
  public void verifyEmail(String rawToken) {
    String hash = sha256Hex(rawToken);
    AuthToken authToken = authTokenRepository
        .findValidToken(hash, AuthTokenType.EMAIL_VERIFY, Instant.now())
        .orElseThrow(
            () -> new InvalidTokenException("The verification token is invalid or has expired."));

    authToken.setUsedAt(Instant.now());
    authToken.getUser().setEmailVerified(true);
    log.info("Email verified for user id={}", authToken.getUser().getId());
  }

  @Override
  public void requestPasswordReset(String email) {
    // Always silently succeed to prevent email enumeration
    userRepository.findByEmail(email.toLowerCase()).ifPresent(user -> {
      AuthToken token = createPasswordResetToken();
      user.addAuthToken(token);
      userRepository.save(user);
      log.info("Password reset token created for user id={}", user.getId());
      // TODO: publish event / send reset email asynchronously
    });
  }

  @Override
  public void confirmPasswordReset(String rawToken, String newPassword) {
    String hash = sha256Hex(rawToken);
    AuthToken authToken = authTokenRepository
        .findValidToken(hash, AuthTokenType.PASSWORD_RESET, Instant.now())
        .orElseThrow(
            () -> new InvalidTokenException("The password reset token is invalid or has expired."));

    User user = authToken.getUser();
    user.setPasswordHash(BCrypt.withDefaults().hashToString(12, newPassword.toCharArray()));
    authToken.setUsedAt(Instant.now());

    refreshTokenRepository.revokeAllForUser(user);
    log.info("Password reset completed for user id={}", user.getId());
  }
  
  // TODO move to a separate service
  private TokenPair issueTokenPair(User user) {
    long expirySeconds = authProperties.getAccessTokenExpirySeconds();
    Instant now = Instant.now();
    Instant accessExpiry = now.plusSeconds(expirySeconds);

    String accessJwt = Jwts.builder()
        .subject(user.getId().toString())
        .claim("email", user.getEmail())
        .issuedAt(Date.from(now))
        .expiration(Date.from(accessExpiry))
        .signWith(jwtSecretKey())
        .compact();

    String rawRefresh = generateOpaqueToken();
    String refreshHash = sha256Hex(rawRefresh);
    Instant refreshExpiry = now.plusSeconds(authProperties.getRefreshTokenExpirySeconds());
    RefreshToken rt = new RefreshToken(refreshHash, refreshExpiry);
    user.addRefreshToken(rt);
    userRepository.save(user);

    return new TokenPair()
        .accessToken(accessJwt)
        .refreshToken(rawRefresh)
        .expiresIn((int) expirySeconds);
  }

  private AuthToken createEmailVerifyToken() {
    String raw = generateOpaqueToken();
    String hash = sha256Hex(raw);
    Instant expires = Instant.now().plusSeconds(authProperties.getEmailVerifyTokenExpirySeconds());
    return new AuthToken(hash, AuthTokenType.EMAIL_VERIFY, expires);
  }

  private AuthToken createPasswordResetToken() {
    String raw = generateOpaqueToken();
    String hash = sha256Hex(raw);
    Instant expires = Instant.now()
        .plusSeconds(authProperties.getPasswordResetTokenExpirySeconds());
    return new AuthToken(hash, AuthTokenType.PASSWORD_RESET, expires);
  }

  private String generateOpaqueToken() {
    byte[] bytes = new byte[32];
    secureRandom.nextBytes(bytes);
    return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
  }

  private String sha256Hex(String input) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
      return HexFormat.of().formatHex(hash);
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException("SHA-256 not available", e);
    }
  }

  private SecretKey jwtSecretKey() {
    byte[] keyBytes = authProperties.getJwtSecret().getBytes(StandardCharsets.UTF_8);
    return Keys.hmacShaKeyFor(keyBytes);
  }
}


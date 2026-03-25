package com.meradeya.app.service.impl;

import com.meradeya.app.domain.entity.AuthToken;
import com.meradeya.app.domain.entity.AuthTokenType;
import com.meradeya.app.domain.entity.RefreshToken;
import com.meradeya.app.domain.entity.User;
import com.meradeya.app.domain.repository.AuthTokenRepository;
import com.meradeya.app.domain.repository.RefreshTokenRepository;
import com.meradeya.app.dto.auth.TokenPair;
import com.meradeya.app.exception.InvalidTokenException;
import com.meradeya.app.prop.AuthProperties;
import com.meradeya.app.service.face.TokenService;
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
public class TokenServiceImpl implements TokenService {

  private final SecureRandom secureRandom = new SecureRandom();
  
  private final AuthTokenRepository authTokenRepository;
  private final RefreshTokenRepository refreshTokenRepository;
  private final AuthProperties authProperties;

  
  @Override
  public TokenPair createTokenPair(User user) {
    long expirySeconds = authProperties.getAccessTokenExpirySeconds();
    Instant now = Instant.now();
    Instant accessExpiry = now.plusSeconds(expirySeconds);

    String accessJwt = buildJwt(user, now, accessExpiry);

    String rawRefresh = generateOpaqueToken();
    String refreshHash = sha256Hex(rawRefresh);
    Instant refreshExpiry = now.plusSeconds(authProperties.getRefreshTokenExpirySeconds());

    RefreshToken rt = new RefreshToken(refreshHash, refreshExpiry);
    user.addRefreshToken(rt);

    return new TokenPair(accessJwt, rawRefresh, (int) expirySeconds);
  }
  
  @Override
  public void revokeRefreshToken(String rawRefreshToken) {
    String hash = sha256Hex(rawRefreshToken);
    refreshTokenRepository.findActiveByHash(hash)
        .ifPresent(rt -> rt.setRevoked(true));
    // Idempotent – no error if not found
  }

  @Override
  public TokenPair rotateRefreshToken(String rawRefreshToken) {
    String hash = sha256Hex(rawRefreshToken);
    RefreshToken rt = refreshTokenRepository.findActiveByHash(hash)
        .orElseThrow(InvalidTokenException::new);

    rt.setRevoked(true);
    return createTokenPair(rt.getUser());
  }

  @Override
  public void revokeAllRefreshTokens(User user) {
    refreshTokenRepository.revokeAllForUser(user);
  }
  
  @Override
  public AuthToken createEmailVerifyToken() {
    String raw = generateOpaqueToken();
    String hash = sha256Hex(raw);
    Instant expires = Instant.now().plusSeconds(authProperties.getEmailVerifyTokenExpirySeconds());
    return new AuthToken(hash, AuthTokenType.EMAIL_VERIFY, expires);
  }

  @Override
  public AuthToken createPasswordResetToken() {
    String raw = generateOpaqueToken();
    String hash = sha256Hex(raw);
    Instant expires = Instant.now()
        .plusSeconds(authProperties.getPasswordResetTokenExpirySeconds());
    return new AuthToken(hash, AuthTokenType.PASSWORD_RESET, expires);
  }
  
  @Override
  public AuthToken consumeEmailVerifyToken(String rawToken) {
    String hash = sha256Hex(rawToken);
    AuthToken token = authTokenRepository
        .findValidToken(hash, AuthTokenType.EMAIL_VERIFY, Instant.now())
        .orElseThrow(
            () -> new InvalidTokenException("The verification token is invalid or has expired."));
    token.setUsedAt(Instant.now());
    return token;
  }

  @Override
  public AuthToken consumePasswordResetToken(String rawToken) {
    String hash = sha256Hex(rawToken);
    AuthToken token = authTokenRepository
        .findValidToken(hash, AuthTokenType.PASSWORD_RESET, Instant.now())
        .orElseThrow(
            () -> new InvalidTokenException("The password reset token is invalid or has expired."));
    token.setUsedAt(Instant.now());
    return token;
  }

  private String buildJwt(User user, Instant now, Instant accessExpiry) {
    return Jwts.builder()
        .subject(user.getId().toString())
        .claim("email", user.getEmail())
        .issuedAt(Date.from(now))
        .expiration(Date.from(accessExpiry))
        .signWith(jwtSecretKey())
        .compact();
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


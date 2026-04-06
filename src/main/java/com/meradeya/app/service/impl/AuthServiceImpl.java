package com.meradeya.app.service.impl;

import at.favre.lib.crypto.bcrypt.BCrypt;
import com.meradeya.app.domain.entity.AuthToken;
import com.meradeya.app.domain.entity.User;
import com.meradeya.app.domain.entity.UserProfile;
import com.meradeya.app.domain.entity.UserStatus;
import com.meradeya.app.domain.repository.UserRepository;
import com.meradeya.app.dto.auth.RegisterResponse;
import com.meradeya.app.dto.auth.TokenPair;
import com.meradeya.app.exception.AccountSuspendedException;
import com.meradeya.app.exception.EmailAlreadyExistsException;
import com.meradeya.app.exception.InvalidCredentialsException;
import com.meradeya.app.service.face.AuthService;
import com.meradeya.app.service.face.TokenService;
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
  private final TokenService tokenService;

  @Override
  public RegisterResponse registerUser(String email, String rawPassword, String displayName) {
    String normalizedEmail = email.toLowerCase();
    if (userRepository.existsByEmail(normalizedEmail)) {
      throw new EmailAlreadyExistsException();
    }

    String passwordHash = hashPassword(rawPassword);
    User user = new User(normalizedEmail, passwordHash);
    UserProfile profile = new UserProfile(user, displayName);
    user.setProfile(profile);
    user.addAuthToken(tokenService.createEmailVerifyToken());
    userRepository.save(user);

    log.info("Registered new user id={} email={}", user.getId(), normalizedEmail);
    // TODO: publish domain event / send verification email via async event

    TokenPair tokens = tokenService.createTokenPair(user);
    return new RegisterResponse(
        user.getId(),
        user.getEmail(),
        tokens.accessToken(),
        tokens.refreshToken(),
        tokens.expiresIn());
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

    return tokenService.createTokenPair(user);
  }

  @Override
  public void logout(String rawRefreshToken) {
    tokenService.revokeRefreshToken(rawRefreshToken);
  }

  @Override
  public TokenPair refresh(String rawRefreshToken) {
    return tokenService.rotateRefreshToken(rawRefreshToken);
  }

  @Override
  public void verifyEmail(String rawToken) {
    AuthToken authToken = tokenService.consumeEmailVerifyToken(rawToken);
    authToken.getUser().setEmailVerified(true);
    log.info("Email verified for user id={}", authToken.getUser().getId());
  }

  @Override
  public void requestPasswordReset(String email) {
    // Always silently succeed to prevent email enumeration
    userRepository.findByEmail(email.toLowerCase()).ifPresent(user -> {
      user.addAuthToken(tokenService.createPasswordResetToken());
      userRepository.save(user);
      log.info("Password reset token created for user id={}", user.getId());
      // TODO: publish event / send reset email asynchronously
    });
  }

  @Override
  public void confirmPasswordReset(String rawToken, String newPassword) {
    var authToken = tokenService.consumePasswordResetToken(rawToken);
    User user = authToken.getUser();
    user.setPasswordHash(hashPassword(newPassword));
    tokenService.revokeAllRefreshTokens(user);
    log.info("Password reset completed for user id={}", user.getId());
  }

  private String hashPassword(String newPassword) {
    return BCrypt.withDefaults().hashToString(12, newPassword.toCharArray());
  }
  
}

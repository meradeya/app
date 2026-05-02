package com.meradeya.app.controller.impl;

import com.meradeya.app.controller.api.AuthControllerApi;
import com.meradeya.app.dto.auth.ConfirmPasswordResetRequest;
import com.meradeya.app.dto.auth.LoginRequest;
import com.meradeya.app.dto.auth.RefreshTokenRequest;
import com.meradeya.app.dto.auth.RegisterRequest;
import com.meradeya.app.dto.auth.RegisterResponse;
import com.meradeya.app.dto.auth.RequestPasswordResetRequest;
import com.meradeya.app.dto.auth.TokenPair;
import com.meradeya.app.dto.auth.VerifyEmailRequest;
import com.meradeya.app.service.face.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;


@Slf4j
@RestController
@RequiredArgsConstructor
public class AuthController implements AuthControllerApi {

  private final AuthService authService;

  @Override
  public ResponseEntity<RegisterResponse> register(RegisterRequest request) {
    log.info("register");
    RegisterResponse body = authService.registerUser(
        request.email(), request.password(), request.displayName()
    );
    log.info("register end");
    return ResponseEntity.status(HttpStatus.CREATED).body(body);
  }

  @Override
  public ResponseEntity<TokenPair> login(LoginRequest request) {
    log.info("login");
    TokenPair pair = authService.login(request.email(), request.password());
    log.info("login end");
    return ResponseEntity.ok(pair);
  }

  @Override
  public ResponseEntity<TokenPair> refresh(RefreshTokenRequest request) {
    log.info("refresh");
    TokenPair pair = authService.refresh(request.refreshToken());
    log.info("refresh end");
    return ResponseEntity.ok(pair);
  }

  @Override
  public ResponseEntity<Void> logout(RefreshTokenRequest request) {
    log.info("logout");
    authService.logout(request.refreshToken());
    log.info("logout end");
    return ResponseEntity.noContent().build();
  }

  @Override
  public ResponseEntity<Void> verifyEmail(VerifyEmailRequest request) {
    log.info("verifyEmail");
    authService.verifyEmail(request.token());
    log.info("verifyEmail end");
    return ResponseEntity.noContent().build();
  }

  @Override
  public ResponseEntity<Void> requestPasswordReset(RequestPasswordResetRequest request) {
    log.info("requestPasswordReset");
    authService.requestPasswordReset(request.email());
    log.info("requestPasswordReset end");
    return ResponseEntity.noContent().build();
  }

  @Override
  public ResponseEntity<Void> confirmPasswordReset(ConfirmPasswordResetRequest request) {
    log.info("confirmPasswordReset");
    authService.confirmPasswordReset(request.token(), request.newPassword());
    log.info("confirmPasswordReset end");
    return ResponseEntity.noContent().build();
  }
}

package com.meradeya.app.service.face;

import com.meradeya.app.generated.api.model.RegisterResponse;
import com.meradeya.app.generated.api.model.TokenPair;

public interface AuthService {

  RegisterResponse registerUser(String email, String rawPassword, String displayName);

  TokenPair login(String email, String rawPassword);

  void logout(String rawRefreshToken);

  TokenPair refresh(String rawRefreshToken);

  void verifyEmail(String rawToken);

  void requestPasswordReset(String email);

  void confirmPasswordReset(String rawToken, String newPassword);
}


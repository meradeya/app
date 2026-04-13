package com.meradeya.app.security;

import com.meradeya.app.security.face.CurrentUserProvider;
import java.util.UUID;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class SecurityContextCurrentUserProvider implements CurrentUserProvider {

  @Override
  public UUID currentUserId() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    Object principal = authentication == null ? null : authentication.getPrincipal();
    if (!(principal instanceof AppUserPrincipal appUserPrincipal)) {
      throw new AccessDeniedException("Authenticated user is required");
    }
    return appUserPrincipal.getUserId();
  }

}

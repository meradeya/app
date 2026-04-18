package com.meradeya.app.security;

import com.meradeya.app.security.face.CurrentUserProvider;
import java.util.UUID;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Reads the current authenticated user id from Spring Security context.
 *
 * <p>Expected principal type is {@link AppUserPrincipal} populated by JWT authentication.
 *
 * @apiNote Intended for service-layer ownership checks where path/resource ids must match the
 * authenticated caller.
 */
@Component
public class SecurityContextCurrentUserProvider implements CurrentUserProvider {

  /**
   * Returns the authenticated user id from the current security context.
   *
   * @return authenticated user id
   * @throws AccessDeniedException when authentication is absent or principal type is unsupported
   * @implSpec Fails closed: any non-{@link AppUserPrincipal} principal is treated as
   * unauthenticated.
   */
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

package com.meradeya.app.security.face;

import java.util.UUID;

/**
 * Provides identity of the currently authenticated user.
 *
 * @apiNote Implementations are expected to read identity from the active security context.
 */
public interface CurrentUserProvider {

  /**
   * Returns the current authenticated user id.
   *
   * @return authenticated user id
   * @throws org.springframework.security.access.AccessDeniedException when no authenticated user is
   *                                                                   available
   */
  UUID currentUserId();

}

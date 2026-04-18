package com.meradeya.app.security;

import com.meradeya.app.domain.entity.User;
import com.meradeya.app.domain.repository.UserRepository;
import com.meradeya.app.util.UserUtils;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Application-specific {@link UserDetailsService} implementation.
 *
 * <p>Converts persisted {@link User} entities into {@link AppUserPrincipal} instances used by
 * Spring
 * Security.
 *
 * @apiNote This service is used in two distinct flows:
 * <ul>
 *   <li>credential login via {@link #loadUserByUsername(String)},</li>
 *   <li>JWT subject re-hydration via {@link #loadByUserId(UUID)}.</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
public class AppUserDetailsService implements UserDetailsService {

  private final UserRepository userRepository;

  /**
   * Loads a user by logical username credential.
   *
   * @param username user credential presented during authentication
   * @return security principal for the matched user
   * @throws UsernameNotFoundException when no user matches the provided value
   * @implNote This implementation currently resolves users by normalized email.
   * @implSpec Returned principal is immutable and safe to cache in the security context for the
   * current request.
   */
  @Override
  @Transactional(readOnly = true)
  public @NonNull UserDetails loadUserByUsername(@NonNull String username)
      throws UsernameNotFoundException {
    return userRepository.findByEmail(UserUtils.normalizeEmail(username))
        .map(AppUserPrincipal::from)
        .orElseThrow(() -> new UsernameNotFoundException("User not found for email: " + username));
  }

  /**
   * Loads a user by immutable user id.
   *
   * @param userId user id stored in JWT subject claim
   * @return security principal for the matched user
   * @throws UsernameNotFoundException when no user exists for the id
   * @apiNote Used by the JWT filter after token validation to obtain current account state.
   */
  @Transactional(readOnly = true)
  public @NonNull UserDetails loadByUserId(@NonNull UUID userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new UsernameNotFoundException("User not found for id: " + userId));
    return AppUserPrincipal.from(user);
  }

}

package com.meradeya.app.security;

import com.meradeya.app.domain.entity.User;
import com.meradeya.app.domain.repository.UserRepository;
import java.util.Locale;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AppUserDetailsService implements UserDetailsService {

  private final UserRepository userRepository;

  @Override
  @Transactional(readOnly = true)
  public @NonNull UserDetails loadUserByUsername(@NonNull String username)
      throws UsernameNotFoundException {
    return userRepository.findByEmail(normalizeEmail(username))
        .map(AppUserPrincipal::from)
        .orElseThrow(() -> new UsernameNotFoundException("User not found for email: " + username));
  }

  @Transactional(readOnly = true)
  public @NonNull UserDetails loadByUserId(@NonNull UUID userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new UsernameNotFoundException("User not found for id: " + userId));
    return AppUserPrincipal.from(user);
  }

  private String normalizeEmail(String email) {
    return email.toLowerCase(Locale.ROOT);
  }

}

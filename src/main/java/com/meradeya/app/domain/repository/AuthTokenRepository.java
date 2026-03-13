package com.meradeya.app.domain.repository;

import com.meradeya.app.domain.entity.AuthToken;
import com.meradeya.app.domain.entity.AuthTokenType;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface AuthTokenRepository extends JpaRepository<AuthToken, UUID> {

  /**
   * Find a token by its hash, type, not yet used, and not expired.
   */
  @Query("""
      SELECT t FROM AuthToken t
      WHERE t.tokenHash = :tokenHash
        AND t.type = :type
        AND t.usedAt IS NULL
        AND t.expiresAt > :now
      """)
  Optional<AuthToken> findValidToken(String tokenHash, AuthTokenType type, Instant now);
}


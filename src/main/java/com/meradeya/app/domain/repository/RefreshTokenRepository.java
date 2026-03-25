package com.meradeya.app.domain.repository;

import com.meradeya.app.domain.entity.RefreshToken;
import com.meradeya.app.domain.entity.User;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

  /**
   * Find an active (not revoked, not expired) refresh token by its hash.
   */
  @Query("""
      SELECT r FROM RefreshToken r
      WHERE r.tokenHash = :tokenHash
        AND r.revoked = false
        AND r.expiresAt > CURRENT_TIMESTAMP
      """)
  Optional<RefreshToken> findActiveByHash(String tokenHash);

  /**
   * Revoke all refresh tokens for a user (used after password reset).
   */
  @Modifying
  @Query("UPDATE RefreshToken r SET r.revoked = true WHERE r.user = :user AND r.revoked = false")
  void revokeAllForUser(User user);
}


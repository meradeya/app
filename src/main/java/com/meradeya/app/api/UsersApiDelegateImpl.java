package com.meradeya.app.api;

import com.meradeya.app.generated.api.UsersApiDelegate;
import com.meradeya.app.generated.api.model.ListingSummaryPage;
import com.meradeya.app.generated.api.model.MyProfile;
import com.meradeya.app.generated.api.model.PublicProfile;
import com.meradeya.app.generated.api.model.UpdateProfileRequest;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

/**
 * Stub implementation of {@link UsersApiDelegate}. Replace method bodies with real business logic
 * after spring security is integrated
 */
@Service
public class UsersApiDelegateImpl implements UsersApiDelegate {

  /**
   * GET /users/me Returns the authenticated user's full profile including private fields.
   */
  @Override
  public ResponseEntity<MyProfile> getMyProfile() {
    // TODO: load authenticated user, map to MyProfile
    return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
  }

  /**
   * PATCH /users/me Partial update of the authenticated user's profile.
   */
  @Override
  public ResponseEntity<MyProfile> updateMyProfile(UpdateProfileRequest updateProfileRequest) {
    return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
  }

  /**
   * GET /users/{userId}/profile Returns the public-facing profile of any user (no auth required).
   */
  @Override
  public ResponseEntity<PublicProfile> getPublicProfile(UUID userId) {
    // TODO: load user by ID, map to PublicProfile (exclude private fields)
    return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
  }

  /**
   * GET /users/me/listings Returns paginated listings belonging to the authenticated seller.
   */
  @Override
  public ResponseEntity<ListingSummaryPage> getMyListings(String status, Integer page,
      Integer pageSize) {
    // TODO: load listings for authenticated user with optional status filter
    return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
  }
}

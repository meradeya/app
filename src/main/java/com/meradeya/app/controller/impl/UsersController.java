package com.meradeya.app.controller.impl;

import com.meradeya.app.controller.api.UsersControllerApi;
import com.meradeya.app.dto.user.ListingSummaryPage;
import com.meradeya.app.dto.user.MyProfile;
import com.meradeya.app.dto.user.PublicProfile;
import com.meradeya.app.dto.user.UpdateProfileRequest;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for user-profile endpoints. All OpenAPI annotations live in {@link UsersControllerApi};
 * this class contains only business logic delegation.
 */
@Slf4j
@RestController
public class UsersController implements UsersControllerApi {

  @Override
  public ResponseEntity<MyProfile> getUserProfile(UUID userId) {
    log.info("getUserProfile userId={}", userId);
    // TODO: load user by ID, verify ownership, map to MyProfile
    log.info("getUserProfile done userId={}", userId);
    return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
  }

  @Override
  public ResponseEntity<MyProfile> updateUserProfile(UUID userId, UpdateProfileRequest request) {
    log.info("updateUserProfile userId={}", userId);
    // TODO: partial-update user profile with optimistic locking, verify ownership
    log.info("updateUserProfile done userId={}", userId);
    return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
  }

  @Override
  public ResponseEntity<PublicProfile> getPublicProfile(UUID userId) {
    log.info("getPublicProfile userId={}", userId);
    // TODO: load user by ID, map to PublicProfile (exclude private fields)
    log.info("getPublicProfile done userId={}", userId);
    return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
  }

  @Override
  public ResponseEntity<ListingSummaryPage> getUserListings(UUID userId, String status, Integer page,
      Integer pageSize) {
    log.info("getUserListings userId={} status={} page={}", userId, status, page);
    // TODO: load listings for the specified user with optional status filter, verify ownership
    log.info("getUserListings done userId={} status={} page={}", userId, status, page);
    return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
  }
}

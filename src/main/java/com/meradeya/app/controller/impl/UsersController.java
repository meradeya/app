package com.meradeya.app.controller.impl;

import com.meradeya.app.controller.api.UsersControllerApi;
import com.meradeya.app.dto.user.ListingSummary;
import com.meradeya.app.dto.user.MyProfile;
import com.meradeya.app.dto.user.PublicProfile;
import com.meradeya.app.dto.user.UpdateProfileRequest;
import com.meradeya.app.service.face.UserService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for user-profile endpoints. All OpenAPI annotations live in {@link UsersControllerApi};
 * this class contains only business logic delegation.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class UsersController implements UsersControllerApi {

  private final UserService userService;

  @Override
  public ResponseEntity<MyProfile> getUserProfile(UUID userId) {
    log.info("getUserProfile userId={}", userId);
    MyProfile profile = userService.getUserProfile(userId);
    log.info("getUserProfile done userId={}", userId);
    return ResponseEntity.ok(profile);
  }

  @Override
  public ResponseEntity<MyProfile> updateUserProfile(UUID userId, UpdateProfileRequest request) {
    log.info("updateUserProfile userId={}", userId);
    MyProfile profile = userService.updateUserProfile(userId, request);
    log.info("updateUserProfile done userId={}", userId);
    return ResponseEntity.ok(profile);
  }

  @Override
  public ResponseEntity<PublicProfile> getPublicProfile(UUID userId) {
    log.info("getPublicProfile userId={}", userId);
    PublicProfile profile = userService.getPublicProfile(userId);
    log.info("getPublicProfile done userId={}", userId);
    return ResponseEntity.ok(profile);
  }

  @Override
  public ResponseEntity<Page<ListingSummary>> getUserListings(UUID userId, String status, Integer page,
      Integer pageSize) {
    log.info("getUserListings userId={} status={} page={}", userId, status, page);
    Page<ListingSummary> listings = userService.getUserListings(userId, status, page, pageSize);
    log.info("getUserListings done userId={} status={} page={}", userId, status, page);
    return ResponseEntity.ok(listings);
  }
}

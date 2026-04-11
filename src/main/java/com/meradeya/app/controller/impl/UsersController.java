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
 * REST controller for user-profile endpoints. All OpenAPI annotations live in
 * {@link UsersControllerApi}; this class contains only business logic delegation.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class UsersController implements UsersControllerApi {

  private final UserService userService;

  @Override
  public ResponseEntity<MyProfile> getUserProfile() {
    log.info("getUserProfile");
    MyProfile profile = userService.getUserProfile();
    log.info("getUserProfile done");
    return ResponseEntity.ok(profile);
  }

  @Override
  public ResponseEntity<MyProfile> updateUserProfile(UpdateProfileRequest request) {
    log.info("updateUserProfile");
    MyProfile profile = userService.updateUserProfile(request);
    log.info("updateUserProfile done");
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
  public ResponseEntity<Page<ListingSummary>> getUserListings(String status, Integer page,
      Integer pageSize) {
    log.info("getUserListings status={} page={}", status, page);
    Page<ListingSummary> listings = userService.getUserListings(status, page, pageSize);
    log.info("getUserListings done status={} page={}", status, page);
    return ResponseEntity.ok(listings);
  }
}

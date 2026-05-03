package com.meradeya.app.controller.impl;

import com.meradeya.app.controller.api.UsersControllerApi;
import com.meradeya.app.dto.user.MyProfile;
import com.meradeya.app.dto.user.PublicProfile;
import com.meradeya.app.dto.user.UpdateProfileRequest;
import com.meradeya.app.service.face.UserService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller implementation for {@link UsersControllerApi}.
 *
 * <p>OpenAPI annotations remain in the API interface; this class focuses on request logging and
 * service
 * delegation.
 *
 * @implNote Security decisions are enforced in lower layers (security filters and service ownership
 * checks), keeping controller methods thin and deterministic.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class UsersController implements UsersControllerApi {

  private final UserService userService;

  /**
   * Delegates private profile retrieval for a specific user id.
   *
   * @param userId target user id from request path
   * @return HTTP 200 with private profile payload
   * @implSpec Method adds request/response boundary logs and delegates business rules to
   * {@link UserService}.
   */
  @Override
  public ResponseEntity<MyProfile> getUserProfile(UUID userId) {
    log.info("getUserProfile");
    MyProfile profile = userService.getUserProfile(userId);
    log.info("getUserProfile end");
    return ResponseEntity.ok(profile);
  }

  /**
   * Delegates partial profile update for a specific user id.
   *
   * @param userId  target user id from request path
   * @param request partial update payload
   * @return HTTP 200 with updated profile payload
   * @implSpec Validation, ownership checks, and optimistic locking are enforced in the service
   * layer.
   */
  @Override
  public ResponseEntity<MyProfile> updateUserProfile(UUID userId, UpdateProfileRequest request) {
    log.info("updateUserProfile");
    MyProfile profile = userService.updateUserProfile(userId, request);
    log.info("updateUserProfile end");
    return ResponseEntity.ok(profile);
  }

  /**
   * Delegates public profile retrieval for a specific user id.
   *
   * @param userId target user id from request path
   * @return HTTP 200 with public profile payload
   */
  @Override
  public ResponseEntity<PublicProfile> getPublicProfile(UUID userId) {
    log.info("getPublicProfile");
    PublicProfile profile = userService.getPublicProfile(userId);
    log.info("getPublicProfile end");
    return ResponseEntity.ok(profile);
  }

}

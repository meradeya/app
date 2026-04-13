package com.meradeya.app.service.impl;

import com.meradeya.app.dto.user.ListingSummary;
import com.meradeya.app.dto.user.MyProfile;
import com.meradeya.app.dto.user.PublicProfile;
import com.meradeya.app.dto.user.UpdateProfileRequest;
import com.meradeya.app.security.face.CurrentUserProvider;
import com.meradeya.app.service.face.UserService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

  private final CurrentUserProvider currentUserProvider;

  @Override
  public MyProfile getUserProfile(UUID requestedUserId) {
    UUID authenticatedUserId = requireOwnerAccess(requestedUserId);
    throw notImplemented(
        "Loading user profile is not implemented yet for userId=" + authenticatedUserId);
  }

  @Override
  public MyProfile updateUserProfile(UUID requestedUserId, UpdateProfileRequest request) {
    UUID authenticatedUserId = requireOwnerAccess(requestedUserId);
    throw notImplemented(
        "Updating user profile is not implemented yet for userId=" + authenticatedUserId);
  }

  @Override
  public PublicProfile getPublicProfile(UUID requestedUserId) {
    throw notImplemented("Loading public profile is not implemented yet");
  }

  @Override
  public Page<ListingSummary> getUserListings(UUID requestedUserId, String status, Integer page,
      Integer pageSize) {
    UUID authenticatedUserId = requireOwnerAccess(requestedUserId);
    throw notImplemented(
        "Loading user listings is not implemented yet for userId=" + authenticatedUserId);
  }

  private UUID requireOwnerAccess(UUID requestedUserId) {
    UUID authenticatedUserId = currentUserProvider.currentUserId();
    if (!authenticatedUserId.equals(requestedUserId)) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN,
          "You can only access your own user resources");
    }
    return authenticatedUserId;
  }

  private ResponseStatusException notImplemented(String message) {
    return new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED, message);
  }

}

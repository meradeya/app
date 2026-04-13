package com.meradeya.app.service.helper;

import com.meradeya.app.domain.entity.Listing;
import com.meradeya.app.domain.entity.ListingPhoto;
import com.meradeya.app.domain.entity.User;
import com.meradeya.app.domain.entity.UserProfile;
import com.meradeya.app.domain.repository.UserRepository;
import com.meradeya.app.dto.user.ListingSummary;
import com.meradeya.app.dto.user.MyProfile;
import com.meradeya.app.dto.user.PublicProfile;
import com.meradeya.app.dto.user.UpdateProfileRequest;
import com.meradeya.app.security.face.CurrentUserProvider;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Comparator;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

/**
 * Shared helper methods for {@code UserService} implementations.
 *
 * <p>Centralizes ownership checks, entity loading, and mapping logic to keep services compact and
 * consistent.
 *
 * @implNote Helper methods translate domain-level failures into HTTP-friendly
 * {@link ResponseStatusException} instances used by REST flows.
 */
@Component
@RequiredArgsConstructor
public class UserServiceHelper {

  private final CurrentUserProvider currentUserProvider;
  private final UserRepository userRepository;


  /**
   * Verifies that a requested user id matches the authenticated user.
   *
   * @param requestedUserId user id supplied by the caller
   * @return authenticated user id when the ownership check succeeds
   * @throws ResponseStatusException with HTTP 403 when caller tries to access another user's
   *                                 resource
   * @apiNote This is the primary guard against horizontal privilege escalation.
   */
  public UUID requireOwnerAccess(UUID requestedUserId) {
    UUID authenticatedUserId = currentUserProvider.currentUserId();
    if (!authenticatedUserId.equals(requestedUserId)) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN,
          "You can only access your own user resources");
    }
    return authenticatedUserId;
  }

  /**
   * Loads a user by id or fails with HTTP 404.
   *
   * @param userId target user id
   * @return persisted user entity
   * @throws ResponseStatusException when the user does not exist
   */
  public User findUserOrThrow(UUID userId) {
    return userRepository.findById(userId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
  }

  /**
   * Checks whether a user exists.
   *
   * @param userId user id to verify
   * @throws ResponseStatusException when the user does not exist
   */
  public void ensureUserExists(UUID userId) {
    if (!userRepository.existsById(userId)) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
    }
  }

  /**
   * Extracts the profile from a loaded user.
   *
   * @param user source user entity
   * @return linked profile entity
   * @throws ResponseStatusException when no profile is present
   */
  public UserProfile requireProfile(User user) {
    UserProfile profile = user.getProfile();
    if (profile == null) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User profile not found");
    }
    return profile;
  }

  /**
   * Applies non-null fields from an update request to an existing profile.
   *
   * @param profile mutable profile entity
   * @param request partial update payload
   * @implNote {@code null} values mean "leave unchanged".
   */
  public void updateProfile(UserProfile profile, UpdateProfileRequest request) {
    if (request.displayName() != null) {
      profile.setDisplayName(request.displayName());
    }
    if (request.avatarUrl() != null) {
      profile.setAvatarUrl(request.avatarUrl());
    }
    if (request.location() != null) {
      profile.setLocation(request.location());
    }
    if (request.bio() != null) {
      profile.setBio(request.bio());
    }
  }

  // TODO: Maybe it is time to add MapStruct?

  /**
   * Maps a user entity to the private profile DTO.
   *
   * @param user source user entity
   * @return private profile view
   * @implSpec Requires a non-null linked {@link UserProfile}; otherwise throws 404 via
   * {@link #requireProfile(User)}.
   */
  public MyProfile toMyProfile(User user) {
    UserProfile profile = requireProfile(user);
    return new MyProfile(
        user.getId(),
        user.getEmail(),
        user.isEmailVerified(),
        user.getStatus().name(),
        profile.getDisplayName(),
        profile.getAvatarUrl(),
        profile.getLocation(),
        profile.getBio(),
        OffsetDateTime.ofInstant(profile.getUpdatedAt(), ZoneOffset.UTC),
        profile.getVersion());
  }

  /**
   * Maps a user entity to the public profile DTO.
   *
   * @param user source user entity
   * @return public profile view
   * @implSpec Reuses the same profile source as private mapping but excludes private account
   * fields.
   */
  public PublicProfile toPublicProfile(User user) {
    UserProfile profile = requireProfile(user);
    return new PublicProfile(
        user.getId(),
        profile.getDisplayName(),
        profile.getAvatarUrl(),
        profile.getLocation(),
        profile.getBio());
  }

  /**
   * Maps a listing entity to a summary DTO.
   *
   * @param listing source listing entity
   * @return listing summary
   * @implNote The first photo is selected by the minimum {@code displayOrder} value.
   */
  public ListingSummary toListingSummary(Listing listing) {
    String firstPhotoUrl = listing.getPhotos().stream()
        .min(Comparator.comparingInt(ListingPhoto::getDisplayOrder))
        .map(ListingPhoto::getUrl)
        .orElse(null);

    return new ListingSummary(
        listing.getId(),
        listing.getTitle(),
        listing.getPrice(),
        listing.getCurrency(),
        listing.getCondition().name(),
        listing.getStatus().name(),
        listing.getLocation(),
        firstPhotoUrl,
        OffsetDateTime.ofInstant(listing.getCreatedAt(), ZoneOffset.UTC));
  }


}

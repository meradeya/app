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

@Component
@RequiredArgsConstructor
public class UserServiceHelper {

  private final CurrentUserProvider currentUserProvider;
  private final UserRepository userRepository;


  public UUID requireOwnerAccess(UUID requestedUserId) {
    UUID authenticatedUserId = currentUserProvider.currentUserId();
    if (!authenticatedUserId.equals(requestedUserId)) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN,
          "You can only access your own user resources");
    }
    return authenticatedUserId;
  }

  public User findUserOrThrow(UUID userId) {
    return userRepository.findById(userId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
  }

  public void ensureUserExists(UUID userId) {
    if (!userRepository.existsById(userId)) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
    }
  }

  public UserProfile requireProfile(User user) {
    UserProfile profile = user.getProfile();
    if (profile == null) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User profile not found");
    }
    return profile;
  }

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

  public PublicProfile toPublicProfile(User user) {
    UserProfile profile = requireProfile(user);
    return new PublicProfile(
        user.getId(),
        profile.getDisplayName(),
        profile.getAvatarUrl(),
        profile.getLocation(),
        profile.getBio());
  }

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

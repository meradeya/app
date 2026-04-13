package com.meradeya.app.service.impl;

import com.meradeya.app.domain.entity.Listing;
import com.meradeya.app.domain.entity.ListingStatus;
import com.meradeya.app.domain.entity.User;
import com.meradeya.app.domain.entity.UserProfile;
import com.meradeya.app.domain.repository.ListingRepository;
import com.meradeya.app.domain.repository.UserRepository;
import com.meradeya.app.dto.user.ListingSummary;
import com.meradeya.app.dto.user.MyProfile;
import com.meradeya.app.dto.user.PublicProfile;
import com.meradeya.app.dto.user.UpdateProfileRequest;
import com.meradeya.app.service.face.UserService;
import com.meradeya.app.service.helper.UserServiceHelper;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

  private final UserRepository userRepository;
  private final ListingRepository listingRepository;
  private final UserServiceHelper userServiceHelper;

  @Override
  public MyProfile getUserProfile(UUID requestedUserId) {
    User user = userServiceHelper.findUserOrThrow(
        userServiceHelper.requireOwnerAccess(requestedUserId));
    return userServiceHelper.toMyProfile(user);
  }

  @Override
  @Transactional
  public MyProfile updateUserProfile(UUID requestedUserId, UpdateProfileRequest request) {
    UUID userId = userServiceHelper.requireOwnerAccess(requestedUserId);
    User user = userServiceHelper.findUserOrThrow(userId);
    UserProfile profile = userServiceHelper.requireProfile(user);

    if (request.version() == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          "Profile version is required for update");
    }
    if (!request.version().equals(profile.getVersion())) {
      throw new ResponseStatusException(HttpStatus.CONFLICT,
          "Profile was modified by another request. Re-fetch and retry.");
    }

    userServiceHelper.updateProfile(profile, request);

    try {
      userRepository.flush();
    } catch (OptimisticLockingFailureException ex) {
      throw new ResponseStatusException(HttpStatus.CONFLICT,
          "Profile was modified by another request. Re-fetch and retry.", ex);
    }

    return userServiceHelper.toMyProfile(user);
  }

  @Override
  public PublicProfile getPublicProfile(UUID requestedUserId) {
    User user = userServiceHelper.findUserOrThrow(requestedUserId);
    return userServiceHelper.toPublicProfile(user);
  }

  @Override
  public Page<ListingSummary> getUserListings(UUID requestedUserId, String status, Integer page,
      Integer pageSize) {
    UUID userId = userServiceHelper.requireOwnerAccess(requestedUserId);
    userServiceHelper.ensureUserExists(userId);

    int safePage = page == null ? 0 : page;
    int safePageSize = pageSize == null ? 20 : pageSize;
    if (safePage < 0) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Page must be >= 0");
    }
    // TODO: maxPageSize should be configurable
    if (safePageSize <= 0 || safePageSize > 100) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          "Page size must be between 1 and 100");
    }

    Pageable pageable = PageRequest.of(safePage, safePageSize);
    ListingStatus listingStatus = ListingStatus.from(status);

    Page<Listing> listings = listingStatus == null
        ? listingRepository.findBySellerId(userId, pageable)
        : listingRepository.findBySellerIdAndStatus(userId, listingStatus, pageable);

    return listings.map(userServiceHelper::toListingSummary);
  }

}

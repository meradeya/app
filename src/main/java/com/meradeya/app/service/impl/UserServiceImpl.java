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

/**
 * Default {@link UserService} implementation backed by JPA repositories.
 *
 * <p>The service enforces owner access for private endpoints and delegates entity-to-DTO mapping
 * and
 * shared validation to {@link UserServiceHelper}.
 *
 * @apiNote Private operations are path-id based and must pass ownership checks; public profile
 * reads are intentionally not owner-restricted.
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

  private final UserRepository userRepository;
  private final ListingRepository listingRepository;
  private final UserServiceHelper userServiceHelper;

  /**
   * {@inheritDoc}
   *
   * @throws ResponseStatusException with HTTP 403 on ownership mismatch or 404 when user is
   *                                 missing
   * @implSpec Requires ownership validation before loading private profile data.
   */
  @Override
  public MyProfile getUserProfile(UUID requestedUserId) {
    User user = userServiceHelper.findUserOrThrow(
        userServiceHelper.requireOwnerAccess(requestedUserId));
    return userServiceHelper.toMyProfile(user);
  }

  /**
   * {@inheritDoc}
   *
   * @throws ResponseStatusException with HTTP 400 for missing version, 403 for ownership mismatch,
   *                                 404 when user/profile does not exist, or 409 for
   *                                 stale/concurrent updates
   * @implSpec Performs optimistic-lock validation using the profile version provided by the
   * client.
   * @implNote A repository flush is forced to convert concurrent update races into a 409 response
   * in the current request.
   */
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

  /**
   * {@inheritDoc}
   */
  @Override
  public PublicProfile getPublicProfile(UUID requestedUserId) {
    User user = userServiceHelper.findUserOrThrow(requestedUserId);
    return userServiceHelper.toPublicProfile(user);
  }

  /**
   * {@inheritDoc}
   *
   * @throws ResponseStatusException with HTTP 403 on ownership mismatch, 404 when user is missing,
   *                                 or 400 for invalid paging/filter input
   * @implSpec Returns owner-visible listings only; invalid status or pagination values are rejected
   * with HTTP 400.
   */
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
    ListingStatus listingStatus;

    try {
      listingStatus = ListingStatus.from(status);
    } catch (IllegalArgumentException ex) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          "Invalid listing status: " + status, ex);
    }

    Page<Listing> listings = listingStatus == null
        ? listingRepository.findBySellerId(userId, pageable)
        : listingRepository.findBySellerIdAndStatus(userId, listingStatus, pageable);

    return listings.map(userServiceHelper::toListingSummary);
  }

}

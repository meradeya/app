package com.meradeya.app.service.face;

import com.meradeya.app.dto.user.ListingSummary;
import com.meradeya.app.dto.user.MyProfile;
import com.meradeya.app.dto.user.PublicProfile;
import com.meradeya.app.dto.user.UpdateProfileRequest;
import java.util.UUID;
import org.springframework.data.domain.Page;

/**
 * Business service for user profile and listing operations.
 *
 * @apiNote Private operations are expected to enforce owner-only access using the authenticated
 * principal.
 */
public interface UserService {

  /**
   * Returns the private profile of the requested user.
   *
   * @param requestedUserId user id from request path
   * @return full private profile payload
   */
  MyProfile getUserProfile(UUID requestedUserId);

  /**
   * Applies a partial profile update for the requested user.
   *
   * @param requestedUserId user id from request path
   * @param request         partial update payload with optimistic-lock version
   * @return updated private profile payload
   */
  MyProfile updateUserProfile(UUID requestedUserId, UpdateProfileRequest request);

  /**
   * Returns the public profile of the requested user.
   *
   * @param requestedUserId target user id
   * @return public profile payload
   */
  PublicProfile getPublicProfile(UUID requestedUserId);

  /**
   * Returns paginated listings for the requested owner.
   *
   * @param requestedUserId user id from request path
   * @param status          optional status filter
   * @param page            zero-based page index
   * @param pageSize        requested page size
   * @return page of listing summaries
   */
  Page<ListingSummary> getUserListings(UUID requestedUserId, String status, Integer page,
      Integer pageSize);

}

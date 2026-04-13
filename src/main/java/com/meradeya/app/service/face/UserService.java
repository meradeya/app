package com.meradeya.app.service.face;

import com.meradeya.app.dto.user.ListingSummary;
import com.meradeya.app.dto.user.MyProfile;
import com.meradeya.app.dto.user.PublicProfile;
import com.meradeya.app.dto.user.UpdateProfileRequest;
import java.util.UUID;
import org.springframework.data.domain.Page;

public interface UserService {

  MyProfile getUserProfile(UUID requestedUserId);

  MyProfile updateUserProfile(UUID requestedUserId, UpdateProfileRequest request);

  PublicProfile getPublicProfile(UUID requestedUserId);

  Page<ListingSummary> getUserListings(UUID requestedUserId, String status, Integer page,
      Integer pageSize);

}

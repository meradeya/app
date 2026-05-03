package com.meradeya.app.controller.api;

import com.meradeya.app.dto.common.ExceptionDetail;
import com.meradeya.app.dto.user.MyProfile;
import com.meradeya.app.dto.user.PublicProfile;
import com.meradeya.app.dto.user.UpdateProfileRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Contract for user profile and listing endpoints.
 *
 * <p>This interface carries OpenAPI metadata and request mappings, while concrete controllers are
 * responsible for orchestration and delegation.
 *
 * @apiNote Private endpoints are owner-restricted: callers must authenticate and reference their
 * own user id.
 */
@Tag(name = "Users", description = "User profiles")
@RequestMapping(value = "/v{version}", version = "1.0")
public interface UsersControllerApi {

  /**
   * Returns the private profile view for the requested user.
   *
   * @param userId identifier from the path; must match the authenticated principal for access
   * @return authenticated user's full profile payload
   */
  @Operation(
      operationId = "getUserProfile",
      summary = "Get user account + profile",
      description = "Returns the specified user's full record including private fields (email, emailVerified, status). Requires authentication and ownership.",
      security = @SecurityRequirement(name = "bearerAuth"),
      responses = {
          @ApiResponse(responseCode = "200", description = "User profile",
              content = @Content(mediaType = "application/json", schema = @Schema(implementation = MyProfile.class))),
          @ApiResponse(responseCode = "401", description = "Not authenticated", content = @Content),
          @ApiResponse(responseCode = "403", description = "Access denied", content = @Content),
          @ApiResponse(responseCode = "404", description = "User not found", content = @Content)
      }
  )
  @GetMapping("/users/{userId}")
  ResponseEntity<MyProfile> getUserProfile(
      @Parameter(name = "userId", description = "Target user's ID", required = true, in = ParameterIn.PATH)
      @PathVariable UUID userId
  );

  /**
   * Applies a partial profile update for the requested user.
   *
   * @param userId  identifier from the path; must match the authenticated principal for access
   * @param request partial update payload including optimistic-lock {@code version}
   * @return updated private profile payload
   * @apiNote Email and password changes are intentionally handled by dedicated authentication
   * flows.
   */
  @Operation(
      operationId = "updateUserProfile",
      summary = "Update user profile",
      description = """
          Partial update of the specified user's profile fields.
          Supply version (from the last GET) for optimistic-lock protection.
          Email and password are changed via dedicated flows.
          Requires authentication and ownership.
          """,
      security = @SecurityRequirement(name = "bearerAuth"),
      responses = {
          @ApiResponse(responseCode = "200", description = "Updated profile",
              content = @Content(mediaType = "application/json", schema = @Schema(implementation = MyProfile.class))),
          @ApiResponse(responseCode = "400", description = "Validation error", content = @Content),
          @ApiResponse(responseCode = "401", description = "Not authenticated", content = @Content),
          @ApiResponse(responseCode = "403", description = "Access denied", content = @Content),
          @ApiResponse(responseCode = "404", description = "User not found", content = @Content),
          @ApiResponse(responseCode = "409", description = "Optimistic lock conflict",
              content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionDetail.class),
                  examples = @ExampleObject(value = "{\"type\":\"about:blank\",\"title\":\"Conflict\",\"status\":409,\"detail\":\"Profile was modified by another request.\"}")))
      }
  )
  @PatchMapping("/users/{userId}")
  ResponseEntity<MyProfile> updateUserProfile(
      @Parameter(name = "userId", description = "Target user's ID", required = true, in = ParameterIn.PATH)
      @PathVariable UUID userId,
      @Valid @RequestBody UpdateProfileRequest request
  );

  /**
   * Returns the public profile view for the requested user.
   *
   * @param userId target user identifier
   * @return public profile payload without private account fields
   */
  @Operation(
      operationId = "getPublicProfile",
      summary = "Get public profile",
      description = """
          Returns the public-facing profile of any user.
          Private fields (email, status) are excluded.
          Works without authentication; useful for seller pages.
          """,
      responses = {
          @ApiResponse(responseCode = "200", description = "Public profile",
              content = @Content(mediaType = "application/json", schema = @Schema(implementation = PublicProfile.class))),
          @ApiResponse(responseCode = "404", description = "User not found", content = @Content)
      }
  )
  @GetMapping("/users/{userId}/profile")
  ResponseEntity<PublicProfile> getPublicProfile(
      @Parameter(name = "userId", description = "Target user's ID", required = true, in = ParameterIn.PATH)
      @PathVariable UUID userId
  );
}

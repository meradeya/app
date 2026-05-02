package com.meradeya.app.controller.api;

import static org.springframework.data.domain.Sort.Direction.DESC;

import com.meradeya.app.domain.entity.ListingStatus;
import com.meradeya.app.dto.category.CategoryTreeDto;
import com.meradeya.app.dto.listing.CreateListingRequest;
import com.meradeya.app.dto.listing.ListingDetail;
import com.meradeya.app.dto.listing.ListingSummary;
import com.meradeya.app.dto.listing.SearchListingRequest;
import com.meradeya.app.dto.listing.UpdateListingRequest;
import com.meradeya.app.security.AppUserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "Listings", description = "Listing discovery, management and photo uploads")
@RequestMapping(value = "/v{version}", version = "1.0")
public interface ListingsControllerApi {

  @Operation(
      operationId = "createListing",
      summary = "Create a new listing",
      description = "Creates a listing in DRAFT status for the authenticated seller.",
      security = @SecurityRequirement(name = "bearerAuth"),
      responses = {
          @ApiResponse(responseCode = "201", description = "Listing created",
              content = @Content(schema = @Schema(implementation = ListingDetail.class))),
          @ApiResponse(responseCode = "400", description = "Validation error", content = @Content),
          @ApiResponse(responseCode = "404", description = "Seller or category not found", content = @Content)
      }
  )
  @PostMapping("/listings")
  ResponseEntity<ListingDetail> createListing(
      @AuthenticationPrincipal AppUserPrincipal principal,
      @Valid @RequestBody CreateListingRequest req
  );

  @Operation(
      operationId = "searchListings",
      summary = "Search public listings",
      description = "Always returns ACTIVE listings only. Supports keyword FTS, price, condition, "
          + "category and sellerId filters. Use sellerId to show listings on a seller's profile page. "
          + "Pagination via standard query params (page, size, sort).",
      responses = {
          @ApiResponse(responseCode = "200", description = "Paginated listing summaries")
      }
  )
  @PostMapping("/listings/search")
  ResponseEntity<Page<ListingSummary>> searchListings(
      @Valid @RequestBody SearchListingRequest req,
      @ParameterObject @PageableDefault(size = 20, sort = "createdAt", direction = DESC) Pageable pageable
  );

  @Operation(
      operationId = "getFeed",
      summary = "Get personalised / recency feed",
      description = "Returns newest ACTIVE listings ordered by created_at DESC. "
          + "Auth optional — authenticated users will receive personalised results in a future iteration.",
      responses = {
          @ApiResponse(responseCode = "200", description = "Paginated feed of listing summaries")
      }
  )
  @GetMapping("/feed")
  ResponseEntity<Page<ListingSummary>> getFeed(
      @ParameterObject @PageableDefault(size = 20, sort = "createdAt", direction = DESC) Pageable pageable
  );

  @Operation(
      operationId = "getOwnListings",
      summary = "Get the authenticated seller's own listings",
      description = "Returns listings for the caller across all statuses. "
          + "Use the optional status filter to narrow to a specific status.",
      security = @SecurityRequirement(name = "bearerAuth"),
      responses = {
          @ApiResponse(responseCode = "200", description = "Paginated listing summaries"),
          @ApiResponse(responseCode = "401", description = "Not authenticated", content = @Content)
      }
  )
  @GetMapping("/listings/own")
  ResponseEntity<Page<ListingSummary>> getOwnListings(
      @AuthenticationPrincipal AppUserPrincipal principal,
      @RequestParam(required = false) ListingStatus status,
      @ParameterObject @PageableDefault(size = 20, sort = "createdAt", direction = DESC) Pageable pageable
  );

  @Operation(
      operationId = "getListing",
      summary = "Get listing detail",
      description = "Returns full detail for an ACTIVE listing. "
          + "Authenticated owners can view their listing in any status.",
      responses = {
          @ApiResponse(responseCode = "200", description = "Listing detail",
              content = @Content(schema = @Schema(implementation = ListingDetail.class))),
          @ApiResponse(responseCode = "404", description = "Not found or not active", content = @Content)
      }
  )
  @GetMapping("/listings/{listingId}")
  ResponseEntity<ListingDetail> getListing(
      @AuthenticationPrincipal AppUserPrincipal principal,
      @PathVariable UUID listingId
  );

  @Operation(
      operationId = "updateListing",
      summary = "Update listing fields (PATCH)",
      description = "Partial update — null fields are ignored. Supply version for optimistic locking.",
      security = @SecurityRequirement(name = "bearerAuth"),
      responses = {
          @ApiResponse(responseCode = "200", description = "Updated listing",
              content = @Content(schema = @Schema(implementation = ListingDetail.class))),
          @ApiResponse(responseCode = "403", description = "Not the listing owner", content = @Content),
          @ApiResponse(responseCode = "404", description = "Listing not found", content = @Content),
          @ApiResponse(responseCode = "409", description = "Optimistic lock conflict — re-fetch and retry", content = @Content)
      }
  )
  @PatchMapping("/listings/{listingId}")
  ResponseEntity<ListingDetail> updateListing(
      @AuthenticationPrincipal AppUserPrincipal principal,
      @PathVariable UUID listingId,
      @Valid @RequestBody UpdateListingRequest req
  );

  @Operation(
      operationId = "publishListing",
      summary = "Publish a listing (DRAFT | ARCHIVED → ACTIVE)",
      security = @SecurityRequirement(name = "bearerAuth"),
      responses = {
          @ApiResponse(responseCode = "200", description = "Published listing",
              content = @Content(schema = @Schema(implementation = ListingDetail.class))),
          @ApiResponse(responseCode = "403", description = "Not the listing owner", content = @Content),
          @ApiResponse(responseCode = "409", description = "Optimistic lock conflict", content = @Content),
          @ApiResponse(responseCode = "422", description = "Invalid status transition", content = @Content)
      }
  )
  @PostMapping("/listings/{listingId}/publish")
  ResponseEntity<ListingDetail> publishListing(
      @AuthenticationPrincipal AppUserPrincipal principal,
      @PathVariable UUID listingId
  );

  @Operation(
      operationId = "archiveListing",
      summary = "Archive a listing (ACTIVE to ARCHIVED)",
      security = @SecurityRequirement(name = "bearerAuth"),
      responses = {
          @ApiResponse(responseCode = "200", description = "Archived listing",
              content = @Content(schema = @Schema(implementation = ListingDetail.class))),
          @ApiResponse(responseCode = "403", description = "Not the listing owner", content = @Content),
          @ApiResponse(responseCode = "409", description = "Optimistic lock conflict", content = @Content),
          @ApiResponse(responseCode = "422", description = "Invalid status transition", content = @Content)
      }
  )
  @PostMapping("/listings/{listingId}/archive")
  ResponseEntity<ListingDetail> archiveListing(
      @AuthenticationPrincipal AppUserPrincipal principal,
      @PathVariable UUID listingId
  );

  @Operation(
      operationId = "deleteListing",
      summary = "Delete a listing (ACTIVE or ARCHIVED to DELETED)",
      security = @SecurityRequirement(name = "bearerAuth"),
      responses = {
          @ApiResponse(responseCode = "200", description = "Deleted listing",
              content = @Content(schema = @Schema(implementation = ListingDetail.class))),
          @ApiResponse(responseCode = "403", description = "Not the listing owner", content = @Content),
          @ApiResponse(responseCode = "409", description = "Optimistic lock conflict", content = @Content),
          @ApiResponse(responseCode = "422", description = "Invalid status transition", content = @Content)
      }
  )
  @PostMapping("/listings/{listingId}/delete")
  ResponseEntity<ListingDetail> deleteListing(
      @AuthenticationPrincipal AppUserPrincipal principal,
      @PathVariable UUID listingId
  );

  @Operation(
      operationId = "uploadPhoto",
      summary = "Upload a photo to a listing",
      security = @SecurityRequirement(name = "bearerAuth"),
      responses = {
          @ApiResponse(responseCode = "200", description = "Updated listing with new photo",
              content = @Content(schema = @Schema(implementation = ListingDetail.class))),
          @ApiResponse(responseCode = "400", description = "Invalid file type or size", content = @Content),
          @ApiResponse(responseCode = "404", description = "Listing not found", content = @Content),
          @ApiResponse(responseCode = "422", description = "Photo limit exceeded", content = @Content)
      }
  )
  @PostMapping(value = "/listings/{listingId}/photos", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  ResponseEntity<ListingDetail> uploadPhoto(
      @PathVariable UUID listingId,
      @RequestPart("file") MultipartFile file
  );

  @Operation(
      operationId = "deletePhoto",
      summary = "Delete a listing photo",
      security = @SecurityRequirement(name = "bearerAuth"),
      responses = {
          @ApiResponse(responseCode = "200", description = "Updated listing after photo removal",
              content = @Content(schema = @Schema(implementation = ListingDetail.class))),
          @ApiResponse(responseCode = "404", description = "Listing or photo not found", content = @Content)
      }
  )
  @DeleteMapping("/listings/{listingId}/photos/{photoId}")
  ResponseEntity<ListingDetail> deletePhoto(
      @PathVariable UUID listingId,
      @PathVariable UUID photoId
  );

  @Operation(
      operationId = "getCategoryTree",
      summary = "Get the full category tree",
      responses = {
          @ApiResponse(responseCode = "200", description = "Category tree")
      }
  )
  @GetMapping("/categories")
  ResponseEntity<List<CategoryTreeDto>> getCategoryTree();
}


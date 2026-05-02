package com.meradeya.app.controller.impl;

import com.meradeya.app.controller.api.ListingsControllerApi;
import com.meradeya.app.domain.entity.ListingStatus;
import com.meradeya.app.dto.category.CategoryTreeDto;
import com.meradeya.app.dto.listing.CreateListingRequest;
import com.meradeya.app.dto.listing.ListingDetail;
import com.meradeya.app.dto.listing.ListingSummary;
import com.meradeya.app.dto.listing.SearchListingRequest;
import com.meradeya.app.dto.listing.UpdateListingRequest;
import com.meradeya.app.security.AppUserPrincipal;
import com.meradeya.app.service.face.ListingService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;


@Slf4j
@RestController
@RequiredArgsConstructor
public class ListingsController implements ListingsControllerApi {

  private final ListingService listingService;

  @Override
  public ResponseEntity<ListingDetail> createListing(
      @AuthenticationPrincipal AppUserPrincipal principal, CreateListingRequest req) {
    log.info("createListing");
    ListingDetail detail = listingService.createListing(principal.getUserId(), req);
    log.info("createListing end");
    return ResponseEntity.status(HttpStatus.CREATED).body(detail);
  }

  @Override
  public ResponseEntity<Page<ListingSummary>> searchListings(SearchListingRequest req, Pageable pageable) {
    log.info("searchListings");
    // TODO: implement full-text search — see HLD §4.3
    log.info("searchListings end");
    return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
  }

  @Override
  public ResponseEntity<Page<ListingSummary>> getFeed(Pageable pageable) {
    log.info("getFeed");
    Page<ListingSummary> result = listingService.getFeed(pageable);
    log.info("getFeed end");
    return ResponseEntity.ok(result);
  }

  @Override
  public ResponseEntity<Page<ListingSummary>> getOwnListings(
      AppUserPrincipal principal, ListingStatus status, Pageable pageable) {
    log.info("getOwnListings");
    Page<ListingSummary> result = listingService.getOwnListings(principal.getUserId(), status, pageable);
    log.info("getOwnListings end");
    return ResponseEntity.ok(result);
  }

  @Override
  public ResponseEntity<ListingDetail> getListing(AppUserPrincipal principal, UUID listingId) {
    log.info("getListing");
    UUID callerId = principal != null ? principal.getUserId() : null;
    ListingDetail detail = listingService.getListing(listingId, callerId);
    log.info("getListing end");
    return ResponseEntity.ok(detail);
  }

  @Override
  public ResponseEntity<ListingDetail> updateListing(
      AppUserPrincipal principal, UUID listingId, UpdateListingRequest req) {
    log.info("updateListing");
    ListingDetail detail = listingService.updateListing(listingId, principal.getUserId(), req);
    log.info("updateListing end");
    return ResponseEntity.ok(detail);
  }

  @Override
  public ResponseEntity<ListingDetail> publishListing(AppUserPrincipal principal, UUID listingId) {
    log.info("publishListing");
    ListingDetail detail = listingService.publish(listingId, principal.getUserId());
    log.info("publishListing end");
    return ResponseEntity.ok(detail);
  }

  @Override
  public ResponseEntity<ListingDetail> archiveListing(AppUserPrincipal principal, UUID listingId) {
    log.info("archiveListing");
    ListingDetail detail = listingService.archive(listingId, principal.getUserId());
    log.info("archiveListing end");
    return ResponseEntity.ok(detail);
  }

  @Override
  public ResponseEntity<ListingDetail> deleteListing(AppUserPrincipal principal, UUID listingId) {
    log.info("deleteListing");
    ListingDetail detail = listingService.deleteListing(listingId, principal.getUserId());
    log.info("deleteListing end");
    return ResponseEntity.ok(detail);
  }

  @Override
  public ResponseEntity<ListingDetail> uploadPhoto(UUID listingId, MultipartFile file) {
    log.info("uploadPhoto");
    ListingDetail detail = listingService.uploadPhoto(listingId, file);
    log.info("uploadPhoto end");
    return ResponseEntity.ok(detail);
  }

  @Override
  public ResponseEntity<ListingDetail> deletePhoto(UUID listingId, UUID photoId) {
    log.info("deletePhoto");
    ListingDetail detail = listingService.deletePhoto(listingId, photoId);
    log.info("deletePhoto end");
    return ResponseEntity.ok(detail);
  }

  @Override
  public ResponseEntity<List<CategoryTreeDto>> getCategoryTree() {
    log.info("getCategoryTree");
    List<CategoryTreeDto> tree = listingService.getCategoryTree();
    log.info("getCategoryTree end");
    return ResponseEntity.ok(tree);
  }
}

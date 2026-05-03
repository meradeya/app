package com.meradeya.app.service.impl;

import com.meradeya.app.domain.entity.Category;
import com.meradeya.app.domain.entity.Listing;
import com.meradeya.app.domain.entity.ListingPhoto;
import com.meradeya.app.domain.entity.ListingStatus;
import com.meradeya.app.domain.entity.User;
import com.meradeya.app.domain.repository.CategoryRepository;
import com.meradeya.app.domain.repository.ListingRepository;
import com.meradeya.app.domain.repository.UserRepository;
import com.meradeya.app.dto.category.CategoryTreeDto;
import com.meradeya.app.dto.listing.CreateListingRequest;
import com.meradeya.app.dto.listing.ListingDetail;
import com.meradeya.app.dto.listing.ListingPhotoDto;
import com.meradeya.app.dto.listing.ListingSummary;
import com.meradeya.app.dto.listing.UpdateListingRequest;
import com.meradeya.app.exception.CategoryNotFoundException;
import com.meradeya.app.exception.InvalidStatusTransitionException;
import com.meradeya.app.exception.ListingNotFoundException;
import com.meradeya.app.exception.OwnerAccessDeniedException;
import com.meradeya.app.exception.PhotoLimitExceededException;
import com.meradeya.app.exception.PhotoNotFoundException;
import com.meradeya.app.exception.UserNotFoundException;
import com.meradeya.app.prop.PhotoStorageProperties;
import com.meradeya.app.service.face.ListingService;
import com.meradeya.app.service.face.PhotoStorageService;
import jakarta.transaction.Transactional;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ListingServiceImpl implements ListingService {

  // Allowed status transitions: from → set of allowed targets
  private static final Map<ListingStatus, Set<ListingStatus>> ALLOWED_TRANSITIONS = Map.of(
      ListingStatus.DRAFT,    Set.of(ListingStatus.ACTIVE),
      ListingStatus.ACTIVE,   Set.of(ListingStatus.ARCHIVED, ListingStatus.DELETED),
      ListingStatus.ARCHIVED, Set.of(ListingStatus.ACTIVE, ListingStatus.DELETED),
      ListingStatus.DELETED,  Set.of()
  );

  private final ListingRepository listingRepository;
  private final CategoryRepository categoryRepository;
  private final UserRepository userRepository;
  private final PhotoStorageService photoStorageService;
  private final PhotoStorageProperties photoStorageProperties;

  @Override
  public ListingDetail createListing(UUID sellerId, CreateListingRequest req) {
    User seller = userRepository.findById(sellerId)
        .orElseThrow(UserNotFoundException::new);
    Category category = categoryRepository.findById(req.categoryId())
        .orElseThrow(CategoryNotFoundException::new);

    Listing listing = Listing.builder()
        .seller(seller)
        .category(category)
        .title(req.title())
        .description(req.description())
        .price(req.price())
        .currency(req.currency())
        .condition(req.condition())
        .location(req.location())
        .attributes(req.attributes())
        .build();
    
    seller.addListing(listing);
    listingRepository.save(listing);
    
    log.info("Created listing {} for seller {}", listing.getId(), sellerId);
    return mapToDetail(listing);
  }

  @Override
  public Page<ListingSummary> getFeed(Pageable pageable) {
    return listingRepository.findByStatus(ListingStatus.ACTIVE, pageable)
        .map(this::mapToSummary);
  }

  @Override
  public Page<ListingSummary> getOwnListings(UUID callerId, ListingStatus status, Pageable pageable) {
    log.info("getOwnListings: callerId={}, status={}", callerId, status);
    Page<ListingSummary> result = (status != null
        ? listingRepository.findBySellerIdAndStatus(callerId, status, pageable)
        : listingRepository.findBySellerId(callerId, pageable))
        .map(this::mapToSummary);
    log.info("getOwnListings: returned {} listings for callerId={}", result.getTotalElements(), callerId);
    return result;
  }

  @Override
  public ListingDetail getListing(UUID listingId, UUID callerId) {
    Listing listing = findListingOrThrow(listingId);
    boolean isOwner = callerId != null && callerId.equals(listing.getSeller().getId());
    // DELETED is always 404 — even for the owner (visible only via /listings/own)
//    if (listing.getStatus() == ListingStatus.DELETED) {
//      throw new ListingNotFoundException();
//    }
    if (!isOwner && listing.getStatus() != ListingStatus.ACTIVE) {
      throw new ListingNotFoundException();
    }
    return mapToDetail(listing);
  }

  @Override
  public ListingDetail updateListing(UUID listingId, UUID callerId, UpdateListingRequest req) {
    Listing listing = findListingOrThrow(listingId);
    requireOwner(listing, callerId);

    if (req.categoryId() != null) {
      Category category = categoryRepository.findById(req.categoryId())
          .orElseThrow(CategoryNotFoundException::new);
      listing.setCategory(category);
    }
    if (req.title() != null) {
      listing.setTitle(req.title());
    }
    if (req.description() != null) {
      listing.setDescription(req.description());
    }
    if (req.price() != null) {
      listing.setPrice(req.price());
    }
    if (req.currency() != null) {
      listing.setCurrency(req.currency());
    }
    if (req.condition() != null) {
      listing.setCondition(req.condition());
    }
    if (req.location() != null) {
      listing.setLocation(req.location());
    }
    if (req.attributes() != null) {
      listing.setAttributes(req.attributes());
    }

    return mapToDetail(listing);
  }

  @Override
  public ListingDetail publish(UUID listingId, UUID callerId) {
    Listing listing = findListingOrThrow(listingId);
    requireOwner(listing, callerId);
    transitionTo(listing, ListingStatus.ACTIVE);
    return mapToDetail(listing);
  }

  @Override
  public ListingDetail archive(UUID listingId, UUID callerId) {
    Listing listing = findListingOrThrow(listingId);
    requireOwner(listing, callerId);
    transitionTo(listing, ListingStatus.ARCHIVED);
    return mapToDetail(listing);
  }

  @Override
  public ListingDetail deleteListing(UUID listingId, UUID callerId) {
    Listing listing = findListingOrThrow(listingId);
    requireOwner(listing, callerId);
    transitionTo(listing, ListingStatus.DELETED);
    return mapToDetail(listing);
  }

  @Override
  public ListingDetail uploadPhoto(UUID listingId, MultipartFile file) {
    Listing listing = findListingOrThrow(listingId);

    // Photos cannot be added to a DELETED listing.
    if (listing.getStatus() == ListingStatus.DELETED) {
      throw new InvalidStatusTransitionException(listing.getStatus().name(),
          "upload photo — photos cannot be added to a DELETED listing");
    }

    long photoCount = listing.getPhotos().size();
    int maxPhotos = photoStorageProperties.getMaxPhotosPerListing();
    if (photoCount >= maxPhotos) {
      throw new PhotoLimitExceededException(maxPhotos);
    }

    String url = photoStorageService.store(listingId, file);
    short order = (short) photoCount;
    ListingPhoto photo = new ListingPhoto(url, order);
    listing.addPhoto(photo);
    listingRepository.save(listing);
    return mapToDetail(listing);
  }

  @Override
  public ListingDetail deletePhoto(UUID listingId, UUID photoId) {
    Listing listing = findListingOrThrow(listingId);

    ListingPhoto photo = listing.getPhotos().stream()
        .filter(p -> p.getId().equals(photoId))
        .findFirst()
        .orElseThrow(PhotoNotFoundException::new);

    photoStorageService.delete(photo.getUrl());
    listing.removePhoto(photo);

    // Compact display_order: re-number remaining photos 0, 1, 2, …
    List<ListingPhoto> remaining = new ArrayList<>(listing.getPhotos());
    remaining.sort(Comparator.comparingInt(ListingPhoto::getDisplayOrder));
    for (short i = 0; i < remaining.size(); i++) {
      remaining.get(i).setDisplayOrder(i);
    }

    listingRepository.save(listing);
    return mapToDetail(listing);
  }

  @Override
  public List<CategoryTreeDto> getCategoryTree() {
    List<Category> all = categoryRepository.findAll();

    // Build id → mutable children list map
    Map<UUID, List<CategoryTreeDto>> childrenMap = new HashMap<>();
    Map<UUID, CategoryTreeDto> nodeMap = new HashMap<>();

    for (Category c : all) {
      childrenMap.put(c.getId(), new ArrayList<>());
    }

    // First pass: create nodes without children yet
    for (Category c : all) {
      CategoryTreeDto node = new CategoryTreeDto(
          c.getId(), c.getName(), c.getSlug(), childrenMap.get(c.getId()));
      nodeMap.put(c.getId(), node);
    }

    // Second pass: wire children
    List<CategoryTreeDto> roots = new ArrayList<>();
    for (Category c : all) {
      if (c.getParent() == null) {
        roots.add(nodeMap.get(c.getId()));
      } else {
        childrenMap.get(c.getParent().getId()).add(nodeMap.get(c.getId()));
      }
    }
    return roots;
  }

  private Listing findListingOrThrow(UUID id) {
    return listingRepository.findById(id).orElseThrow(ListingNotFoundException::new);
  }

  private void requireOwner(Listing listing, UUID callerId) {
    if (callerId == null || !callerId.equals(listing.getSeller().getId())) {
      throw new OwnerAccessDeniedException();
    }
  }

  private void transitionTo(Listing listing, ListingStatus target) {
    Set<ListingStatus> allowed = ALLOWED_TRANSITIONS.getOrDefault(listing.getStatus(), Set.of());
    if (!allowed.contains(target)) {
      throw new InvalidStatusTransitionException(listing.getStatus().name(), target.name());
    }
    ListingStatus previous = listing.getStatus();
    listing.setStatus(target);
    log.info("Listing {} transitioned {} -> {}", listing.getId(), previous, target);
  }


  private ListingSummary mapToSummary(Listing l) {
    String firstPhotoUrl = l.getPhotos().stream()
        .min(Comparator.comparingInt(ListingPhoto::getDisplayOrder))
        .map(ListingPhoto::getUrl)
        .orElse(null);
    return new ListingSummary(
        l.getId(),
        l.getTitle(),
        l.getPrice(),
        l.getCurrency(),
        l.getCondition().name(),
        l.getStatus().name(),
        l.getLocation(),
        firstPhotoUrl,
        toOffsetDateTime(l.getCreatedAt())
    );
  }

  private ListingDetail mapToDetail(Listing l) {
    List<ListingPhotoDto> photos = l.getPhotos().stream()
        .sorted(Comparator.comparingInt(ListingPhoto::getDisplayOrder))
        .map(ListingPhotoDto::new)
        .toList();

    return new ListingDetail(
        l.getId(),
        l.getSeller().getId(),
        l.getCategory().getId(),
        l.getTitle(),
        l.getDescription(),
        l.getPrice(),
        l.getCurrency(),
        l.getCondition().name(),
        l.getStatus().name(),
        l.getLocation(),
        l.getAttributes(),
        photos,
        toOffsetDateTime(l.getCreatedAt()),
        toOffsetDateTime(l.getUpdatedAt()),
        l.getVersion()
    );
  }

  private OffsetDateTime toOffsetDateTime(Instant instant) {
    return instant != null ? OffsetDateTime.ofInstant(instant, ZoneOffset.UTC) : null;
  }
}


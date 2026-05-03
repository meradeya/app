package com.meradeya.app.dto.listing;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Schema(description = "Full detail of a single listing")
public record ListingDetail(
    @Schema(description = "Listing ID") UUID id,
    @Schema(description = "Seller user ID") UUID sellerId,
    @Schema(description = "Category ID") UUID categoryId,
    @Schema(description = "Title") String title,
    @Schema(description = "Description") String description,
    @Schema(description = "Price") BigDecimal price,
    @Schema(description = "Currency code") String currency,
    @Schema(description = "Item condition") String condition,
    @Schema(description = "Listing status") String status,
    @Schema(description = "Seller location") String location,
    @Schema(description = "Custom attributes") Map<String, Object> attributes,
    @Schema(description = "Photos ordered by display order") List<ListingPhotoDto> photos,
    @Schema(description = "Creation timestamp") OffsetDateTime createdAt,
    @Schema(description = "Last update timestamp") OffsetDateTime updatedAt,
    @Schema(description = "Optimistic lock version — echo back for PATCH requests") long version
) {

}

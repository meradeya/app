package com.meradeya.app.dto.listing;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Schema(description = "Summary information about a listing")
public record ListingSummary(
    @Schema(description = "Listing ID") UUID id,
    @Schema(description = "Title") String title,
    @Schema(description = "Price") BigDecimal price,
    @Schema(description = "Currency code") String currency,
    @Schema(description = "Item condition") String condition,
    @Schema(description = "Listing status") String status,
    @Schema(description = "Seller location") String location,
    @Schema(description = "URL of the first photo") String firstPhotoUrl,
    @Schema(description = "Listing creation timestamp") OffsetDateTime createdAt
) {

}

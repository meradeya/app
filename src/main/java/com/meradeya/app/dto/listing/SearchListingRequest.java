package com.meradeya.app.dto.listing;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.UUID;

@Schema(description = "Request body for listing search — always returns ACTIVE listings only")
public record SearchListingRequest(
    @Schema(description = "Full-text search keyword") String q,
    @Schema(description = "Filter by category ID") UUID categoryId,
    @Schema(description = "Filter by condition (NEW, LIKE_NEW, GOOD, FAIR, POOR)") String condition,
    @Schema(description = "Minimum price (inclusive)") BigDecimal minPrice,
    @Schema(description = "Maximum price (inclusive)") BigDecimal maxPrice,
    @Schema(description = "Filter by seller ID — use for seller profile pages") UUID sellerId
) {
}

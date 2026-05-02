package com.meradeya.app.dto.listing;

import com.meradeya.app.domain.entity.ListingCondition;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

@Schema(description = "Request body for partial listing update (PATCH semantics — null fields are ignored)")
public record UpdateListingRequest(
    @Schema(description = "New category ID") UUID categoryId,
    @Size(max = 200) @Schema(description = "New title") String title,
    @Schema(description = "New description") String description,
    @DecimalMin("0.01") @Schema(description = "New price") BigDecimal price,
    @Size(min = 3, max = 3) @Pattern(regexp = "[A-Z]{3}") @Schema(description = "New currency code") String currency,
    @Schema(description = "New condition") ListingCondition condition,
    @Size(max = 200) @Schema(description = "New location") String location,
    @Schema(description = "New attributes map") Map<String, Object> attributes,
    @NotNull @Schema(description = "Current optimistic lock version (from last GET)") Long version
) {

}

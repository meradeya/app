package com.meradeya.app.dto.listing;

import com.meradeya.app.domain.entity.ListingCondition;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

@Schema(description = "Request body to create a new listing")
public record CreateListingRequest(
    @NotNull @Schema(description = "Category ID") UUID categoryId,
    @NotBlank @Size(max = 200) @Schema(description = "Listing title") String title,
    @Schema(description = "Detailed description") String description,
    @NotNull @DecimalMin("0.01") @Schema(description = "Price") BigDecimal price,
    @Size(min = 3, max = 3) @Pattern(regexp = "[A-Z]{3}") @Schema(description = "ISO 4217 currency code, defaults to MDL") String currency,
    @NotNull @Schema(description = "Item condition") ListingCondition condition,
    @Size(max = 200) @Schema(description = "Seller location") String location,
    @Schema(description = "Custom key-value attributes") Map<String, Object> attributes
) {

}

package com.meradeya.app.dto.category;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.UUID;

@Schema(description = "Category node in the category tree")
public record CategoryTreeDto(
    @Schema(description = "Category ID") UUID id,
    @Schema(description = "Category name") String name,
    @Schema(description = "URL slug") String slug,
    @Schema(description = "Child categories") List<CategoryTreeDto> children
) {

}

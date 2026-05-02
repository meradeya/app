package com.meradeya.app.dto.listing;

import com.meradeya.app.domain.entity.ListingPhoto;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

@Schema(description = "A listing photo")
public record ListingPhotoDto(
    @Schema(description = "Photo ID") UUID id,
    @Schema(description = "Server-relative URL of the photo") String url,
    @Schema(description = "Display order (0-based)") short displayOrder
) {

  public ListingPhotoDto(ListingPhoto photo) {
    this(photo.getId(), photo.getUrl(), photo.getDisplayOrder());
  }
}

package com.meradeya.app.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "Paginated page of listing summaries")
public record ListingSummaryPage(
    @Schema(description = "Listing items on this page") List<ListingSummary> data,
    @Schema(description = "Pagination metadata") PaginatedMeta meta
) {

}


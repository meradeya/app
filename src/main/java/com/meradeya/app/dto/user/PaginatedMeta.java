package com.meradeya.app.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Pagination metadata")
public record PaginatedMeta(
    @Schema(description = "Current page (0-based)") Integer page,
    @Schema(description = "Page size") Integer pageSize,
    @Schema(description = "Total number of elements") Integer totalElements,
    @Schema(description = "Total number of pages") Integer totalPages
) {

}


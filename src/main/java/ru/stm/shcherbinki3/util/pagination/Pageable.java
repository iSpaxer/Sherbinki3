package ru.stm.shcherbinki3.util.pagination;

import io.swagger.v3.oas.annotations.media.Schema;

public record Pageable(
        @Schema(description = "Page number (0-based)", defaultValue = "0")
        int page,
        @Schema(description = "Number of items per page", defaultValue = "10")
        int size,
        @Schema(description = "Field to sort by", defaultValue = "id")
        String sortBy,
        @Schema(description = "Sort direction", defaultValue = "ASC")
        SortDirection direction
) {
    public int offset() {
        return page * size;
    }

    public enum SortDirection {
        ASC, DESC
    }
}



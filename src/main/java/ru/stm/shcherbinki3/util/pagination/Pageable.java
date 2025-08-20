package ru.stm.shcherbinki3.util.pagination;

public record Pageable(
        int page,
        int size,
        String sortBy,
        SortDirection direction
) {
    public int offset() {
        return page * size;
    }

    public enum SortDirection {
        ASC, DESC
    }
}



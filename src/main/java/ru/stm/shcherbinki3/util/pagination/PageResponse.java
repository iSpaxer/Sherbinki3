package ru.stm.shcherbinki3.util.pagination;

import java.io.Serializable;
import java.util.List;

public record PageResponse<T> (
        List<T> content,
        int page,
        int size,
        long totalElements
) implements Serializable {
    public int totalPages() {
        return (int) Math.ceil((double) totalElements / size);
    }
}


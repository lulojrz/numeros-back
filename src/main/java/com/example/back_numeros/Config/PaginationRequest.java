package com.example.back_numeros.Config;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

public final class PaginationRequest {

    private PaginationRequest() {
    }

    public static Pageable bounded(int page, int size) {
        return bounded(page, size, 100);
    }

    public static Pageable bounded(int page, int size, int maxSize) {
        return PageRequest.of(Math.max(0, page), Math.max(1, Math.min(size, maxSize)));
    }
}

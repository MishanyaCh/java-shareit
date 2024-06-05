package ru.practicum.shareit.util;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public final class Page {
    public static Pageable getSortedPage(int page, int size, Sort sort) {
        return PageRequest.of(page / size, size, sort);
    }

    public static Pageable getPage(int page, int size) {
        return PageRequest.of(page / size, size);
    }
}

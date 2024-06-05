package ru.practicum.shareit.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import static org.junit.jupiter.api.Assertions.*;

class PageTest {
    private int from;
    private int size;
    private Sort sort;

    @BeforeEach
    void setUp() {
        from = 0;
        size = 2;
        sort = Sort.by(Sort.Direction.DESC, "startBookingDate");
    }

    @Test
    void getSortedPage() {
        Pageable pageable = Page.getSortedPage(from, size, sort);

        assertEquals(0, pageable.getPageNumber());
        assertEquals(2, pageable.getPageSize());
        assertEquals("startBookingDate: DESC", pageable.getSort().toString());
    }

    @Test
    void getPage() {
        Pageable pageable = Page.getPage(from, size);

        assertEquals(0, pageable.getPageNumber());
        assertEquals(2, pageable.getPageSize());
    }
}
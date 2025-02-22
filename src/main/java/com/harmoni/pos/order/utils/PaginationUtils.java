package com.harmoni.pos.order.utils;

import com.github.pagehelper.PageHelper;

public class PaginationUtils {

    private PaginationUtils() {
        throw new IllegalArgumentException("Illegal exception");
    }

    public static void applyPagination(int startPage, int sizePage) {
        int page = Math.max(startPage, 1);
        int size = Math.max(sizePage, 1);
        PageHelper.startPage(page, size, true);
    }
}

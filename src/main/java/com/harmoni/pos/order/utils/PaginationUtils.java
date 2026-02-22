package com.harmoni.pos.order.utils;

import com.github.pagehelper.PageHelper;

/**
 * Utility class for applying pagination settings using the MyBatis PageHelper plugin.
 * <p>
 * This class standardizes pagination behavior across the application.
 * </p>
 */
public class PaginationUtils {

    /**
     * Private constructor to prevent instantiation of this utility class.
     *
     * @throws IllegalArgumentException always thrown to indicate that this is a utility class
     */
    private PaginationUtils() {
        throw new IllegalArgumentException("Illegal exception");
    }

    /**
     * Applies pagination settings using PageHelper with safety checks for page and size values.
     * <p>
     * Ensures that the page and size values are at least 1 to avoid invalid pagination.
     * </p>
     *
     * @param startPage the page number to start from (1-based index)
     * @param sizePage  the number of records per page
     */
    public static void applyPagination(int startPage, int sizePage) {
        int page = Math.max(startPage, 1);
        int size = Math.max(sizePage, 1);
        PageHelper.startPage(page, size, true);
    }
}

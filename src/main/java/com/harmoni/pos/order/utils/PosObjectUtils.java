package com.harmoni.pos.order.utils;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Utility class for object array manipulations used in POS-related operations.
 * <p>
 * Provides a method to append a value to an existing {@code Object[]} array.
 * </p>
 */
public final class PosObjectUtils {

    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private PosObjectUtils() {
    }

    /**
     * Appends a {@link String} value to the end of the given {@code Object[]} array.
     *
     * @param obj   the original object array
     * @param value the string value to append
     * @return a new {@code Object[]} containing the original elements and the appended value
     */
    public static Object[] appendValue(Object[] obj, String value) {
        ArrayList<Object> temp = new ArrayList<>(Arrays.asList(obj));
        temp.add(value);
        return temp.toArray();
    }
}

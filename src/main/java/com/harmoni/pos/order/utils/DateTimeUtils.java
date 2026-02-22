package com.harmoni.pos.order.utils;

import java.time.*;
import java.sql.Timestamp;

/**
 * Utility class for handling date and time-related operations for the POS order system.
 * <p>
 * This class provides methods for calculating time ranges such as the start and end of the current day.
 * </p>
 */
public class DateTimeUtils {

    /**
     * Private constructor to prevent instantiation of this utility class.
     *
     * @throws IllegalStateException always thrown to indicate that this is a utility class
     */
    private DateTimeUtils() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Returns an array containing the start and end timestamps for the current day based on the specified time zone.
     * <p>
     * The start time is set to 00:00:00 and the end time is set to 23:59:59.
     * </p>
     *
     * @param zoneId the {@link ZoneId} representing the time zone to calculate the time range in
     * @return an array of two {@link Timestamp} objects:
     *         <ul>
     *           <li>Index 0 - Start of the day (00:00:00)</li>
     *           <li>Index 1 - End of the day (23:59:59)</li>
     *         </ul>
     */
    public static Timestamp[] getDailyTimeRange(ZoneId zoneId) {
        LocalDate today = LocalDate.now(zoneId);

        LocalDateTime startOfDay = today.atStartOfDay();  // 00:00:00
        LocalDateTime endOfDay = today.atTime(23, 59, 59); // 23:59:59

        return new Timestamp[]{
                Timestamp.valueOf(startOfDay),
                Timestamp.valueOf(endOfDay)
        };
    }
}

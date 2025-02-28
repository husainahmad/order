package com.harmoni.pos.order.utils;

import java.time.*;
import java.sql.Timestamp;

public class DateTimeUtils {
    private DateTimeUtils() {
        throw new IllegalStateException("Utility class");
    }

    public static Timestamp[] getDailyTimeRange(ZoneId zoneId) {
        LocalDate today = LocalDate.now(zoneId);

        LocalDateTime startOfDay = today.atStartOfDay();  // 00:00
        LocalDateTime endOfDay = today.atTime(23, 59, 59); // 23:59:59

        return new Timestamp[]{
                Timestamp.valueOf(startOfDay),
                Timestamp.valueOf(endOfDay)
        };
    }

}

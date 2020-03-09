package com.v8tix.katix.social.util;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

public interface DateHelper {

    static String isoDate(final OffsetDateTime now) {
        final DateTimeFormatter formatter = DateTimeFormatter.ISO_ZONED_DATE_TIME;
        return formatter.format(now);
    }

    static long epochMilli(final OffsetDateTime now) {
        return now.toInstant().toEpochMilli();
    }

    static OffsetDateTime getOffsetDateTime() {
        return OffsetDateTime.now();
    }
}

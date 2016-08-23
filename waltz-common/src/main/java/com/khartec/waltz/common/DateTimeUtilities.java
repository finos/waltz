package com.khartec.waltz.common;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

public class DateTimeUtilities {

    public static final ZoneId UTC = ZoneId.of("UTC");


    public static LocalDateTime nowUtc() {
        return LocalDateTime.now(UTC);
    }


    public static java.sql.Date toSqlDate(Date date) {
        if (date == null) {
            return null;
        }
        return new java.sql.Date(date.getTime());
    }

}

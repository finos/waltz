package com.khartec.waltz.common;

import java.time.LocalDateTime;
import java.time.ZoneId;

public class DateTimeUtilities {

    public static LocalDateTime nowUtc() {
        return LocalDateTime.now(ZoneId.of("UTC"));
    }

}

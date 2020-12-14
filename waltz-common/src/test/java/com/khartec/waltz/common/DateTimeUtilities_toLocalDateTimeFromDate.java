package com.khartec.waltz.common;

import org.junit.Test;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Date;

import static junit.framework.TestCase.assertNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class DateTimeUtilities_toLocalDateTimeFromDate {
    @Test
    public void dateTimeConversionsReturnNullIfGivenNull() {
        assertNull(DateTimeUtilities.toLocalDateTime(null));
    }

    @Test
    public void dateTimeConversionsReturnNotNullIfGivenNotNull() {
        assertNotNull(DateTimeUtilities.toLocalDateTime(Date.from(Instant.now())));
        assertEquals(LocalDateTime.class, DateTimeUtilities.toLocalDateTime(Date.from(Instant.now())).getClass());
    }
}

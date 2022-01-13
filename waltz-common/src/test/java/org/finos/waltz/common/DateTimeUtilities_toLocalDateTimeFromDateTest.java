package org.finos.waltz.common;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

public class DateTimeUtilities_toLocalDateTimeFromDateTest {
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

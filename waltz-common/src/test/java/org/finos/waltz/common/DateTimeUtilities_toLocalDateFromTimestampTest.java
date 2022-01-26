package org.finos.waltz.common;

import org.junit.jupiter.api.Test;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

public class DateTimeUtilities_toLocalDateFromTimestampTest {
    @Test
    public void dateTimeConversionsReturnNullIfGivenNull() {

        assertNull(DateTimeUtilities.toLocalDate(null));

    }

    @Test
    public void dateTimeConversionsReturnNotNullIfGivenNotNull() {
        assertNotNull(DateTimeUtilities.toLocalDate(Timestamp.from(Instant.now())));
        assertEquals(LocalDate.class, DateTimeUtilities.toLocalDate(Timestamp.from(Instant.now())).getClass());
    }
}

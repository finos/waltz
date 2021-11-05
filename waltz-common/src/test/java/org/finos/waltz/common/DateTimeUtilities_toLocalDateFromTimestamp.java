package org.finos.waltz.common;

import org.junit.Test;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;

import static junit.framework.TestCase.assertNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class DateTimeUtilities_toLocalDateFromTimestamp {
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

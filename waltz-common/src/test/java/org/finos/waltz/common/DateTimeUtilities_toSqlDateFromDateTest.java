package org.finos.waltz.common;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.sql.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

public class DateTimeUtilities_toSqlDateFromDateTest {
    @Test
    public void dateTimeConversionsReturnNullIfGivenNull() {
        assertNull(DateTimeUtilities.toSqlDate((Date) null));
    }

    @Test
    public void dateTimeConversionsReturnNotNullIfGivenNotNull() {
        assertNotNull(DateTimeUtilities.toSqlDate(Date.from(Instant.now())));
        assertEquals(Date.class, DateTimeUtilities.toSqlDate(Date.from(Instant.now())).getClass());
    }
}

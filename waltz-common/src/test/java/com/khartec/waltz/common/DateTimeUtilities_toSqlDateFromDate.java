package com.khartec.waltz.common;

import org.junit.Test;

import java.time.Instant;
import java.sql.Date;

import static junit.framework.TestCase.assertNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class DateTimeUtilities_toSqlDateFromDate {
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

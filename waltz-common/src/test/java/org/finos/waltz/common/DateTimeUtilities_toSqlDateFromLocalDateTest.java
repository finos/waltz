package org.finos.waltz.common;

import org.junit.Test;

import java.time.LocalDate;
import java.sql.Date;

import static junit.framework.TestCase.assertNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class DateTimeUtilities_toSqlDateFromLocalDateTest {
    @Test
    public void dateTimeConversionsReturnNullIfGivenNull() {
        assertNull(DateTimeUtilities.toSqlDate((LocalDate) null));
    }

    @Test
    public void dateTimeConversionsReturnNotNullIfGivenNotNull() {
        assertNotNull(DateTimeUtilities.toSqlDate(LocalDate.now()));
        assertEquals(Date.class, DateTimeUtilities.toSqlDate(LocalDate.now()).getClass());
    }
}

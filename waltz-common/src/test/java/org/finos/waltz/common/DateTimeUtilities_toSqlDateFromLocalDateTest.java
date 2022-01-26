package org.finos.waltz.common;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.sql.Date;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

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

package org.finos.waltz.common;

import org.junit.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Date;

import static junit.framework.TestCase.assertNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class DateTimeUtilities_toLocalDateFromDateTest {

    @Test
    public void dateTimeConversionsReturnNullIfGivenNull() {
        assertNull(DateTimeUtilities.toLocalDate((Date) null));
    }

    @Test
    public void dateTimeConversionsReturnNotNullIfGivenNotNull() {
        assertNotNull(DateTimeUtilities.toLocalDate(Date.from(Instant.now())));
        assertEquals(LocalDate.class, DateTimeUtilities.toLocalDate(Date.from(Instant.now())).getClass());
    }
}

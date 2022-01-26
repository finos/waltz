package org.finos.waltz.common;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

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

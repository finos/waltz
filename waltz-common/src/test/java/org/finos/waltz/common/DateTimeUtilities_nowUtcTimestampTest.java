package org.finos.waltz.common;

import org.junit.jupiter.api.Test;

import java.sql.Timestamp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class DateTimeUtilities_nowUtcTimestampTest {
    @Test
    public void getNowUtcTimestamp(){
        assertNotNull(DateTimeUtilities.nowUtcTimestamp());
    }

    @Test
    public void confirmClass(){
        assertEquals(Timestamp.class,DateTimeUtilities.nowUtcTimestamp().getClass());
    }
}

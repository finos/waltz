package org.finos.waltz.common;

import org.junit.Test;

import java.sql.Timestamp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class DateTimeUtilities_nowUtcTimestamp {
    @Test
    public void getNowUtcTimestamp(){
        assertNotNull(DateTimeUtilities.nowUtcTimestamp());
    }

    @Test
    public void confirmClass(){
        assertEquals(Timestamp.class,DateTimeUtilities.nowUtcTimestamp().getClass());
    }
}

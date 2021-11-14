package org.finos.waltz.common;

import org.junit.Test;

import java.time.LocalDateTime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class DateTimeUtilities_nowUtcTest {
    @Test
    public void getNowUtc(){
        assertNotNull(DateTimeUtilities.nowUtc());
    }

    @Test
    public void confirmClass(){
        assertEquals(LocalDateTime.class,DateTimeUtilities.nowUtc().getClass());
    }
}

package org.finos.waltz.common;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

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

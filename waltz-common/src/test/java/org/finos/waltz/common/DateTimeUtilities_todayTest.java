package org.finos.waltz.common;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class DateTimeUtilities_todayTest {

    @Test
    public void getToday(){
        assertNotNull(DateTimeUtilities.today());
    }

    @Test
    public void confirmClass(){
        assertEquals(LocalDate.class,DateTimeUtilities.today().getClass());
    }
}

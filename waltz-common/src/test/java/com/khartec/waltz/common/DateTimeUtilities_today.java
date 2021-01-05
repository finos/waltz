package com.khartec.waltz.common;

import org.junit.Test;

import java.time.LocalDate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class DateTimeUtilities_today {

    @Test
    public void getToday(){
        assertNotNull(DateTimeUtilities.today());
    }

    @Test
    public void confirmClass(){
        assertEquals(LocalDate.class,DateTimeUtilities.today().getClass());
    }
}

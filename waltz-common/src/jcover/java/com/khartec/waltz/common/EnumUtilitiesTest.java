package com.khartec.waltz.common;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertTrue;

import java.time.DayOfWeek;
import java.util.LinkedList;

import org.junit.Test;

/**
 * Unit tests for com.khartec.waltz.common.EnumUtilities
 *
 * @author Diffblue JCover
 */

public class EnumUtilitiesTest {

    @Test
    public void names() {
        assertTrue(EnumUtilities.<DayOfWeek>names(new LinkedList<DayOfWeek>()).isEmpty());
        assertThat(EnumUtilities.<DayOfWeek>names(new DayOfWeek[] { DayOfWeek.MONDAY }).size(), is(1));
        assertTrue(EnumUtilities.<DayOfWeek>names(new DayOfWeek[] { DayOfWeek.MONDAY }).contains("MONDAY"));
    }
}

package com.khartec.waltz.model;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import org.junit.Test;

/**
 * Unit tests for com.khartec.waltz.model.Quarter
 *
 * @author Diffblue JCover
 */

public class QuarterTest {

    @Test
    public void fromIntQuarterIsOneReturnsQ1() {
        assertThat(Quarter.fromInt(1), is(Quarter.Q1));
    }

    @Test
    public void valuesReturnsQ1Q2Q3Q4() {
        Quarter[] result = Quarter.values();
        assertThat(result[0], is(Quarter.Q1));
        assertThat(result[1], is(Quarter.Q2));
        assertThat(result[2], is(Quarter.Q3));
        assertThat(result[3], is(Quarter.Q4));
    }
}

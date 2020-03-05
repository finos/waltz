package com.khartec.waltz.model.performance_metric.sample;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import org.junit.Test;

/**
 * Unit tests for com.khartec.waltz.model.performance_metric.sample.SampleType
 *
 * @author Diffblue JCover
 */

public class SampleTypeTest {

    @Test
    public void valuesReturnsMANUALAUTOMATED() {
        SampleType[] result = SampleType.values();
        assertThat(result[0], is(SampleType.MANUAL));
        assertThat(result[1], is(SampleType.AUTOMATED));
    }
}

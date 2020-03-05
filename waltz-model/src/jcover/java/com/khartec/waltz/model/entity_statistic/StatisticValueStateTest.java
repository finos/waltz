package com.khartec.waltz.model.entity_statistic;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import org.junit.Test;

/**
 * Unit tests for com.khartec.waltz.model.entity_statistic.StatisticValueState
 *
 * @author Diffblue JCover
 */

public class StatisticValueStateTest {

    @Test
    public void valuesReturnsEXEMPTPROVIDEDUNKNOWN() {
        StatisticValueState[] result = StatisticValueState.values();
        assertThat(result[0], is(StatisticValueState.EXEMPT));
        assertThat(result[1], is(StatisticValueState.PROVIDED));
        assertThat(result[2], is(StatisticValueState.UNKNOWN));
    }
}

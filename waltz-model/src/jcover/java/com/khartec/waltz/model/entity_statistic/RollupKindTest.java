package com.khartec.waltz.model.entity_statistic;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import org.junit.Test;

/**
 * Unit tests for com.khartec.waltz.model.entity_statistic.RollupKind
 *
 * @author Diffblue JCover
 */

public class RollupKindTest {

    @Test
    public void valuesReturnsCOUNT_BY_ENTITYSUM_BY_VALUEAVG_BY_VALUENONE() {
        RollupKind[] result = RollupKind.values();
        assertThat(result[0], is(RollupKind.COUNT_BY_ENTITY));
        assertThat(result[1], is(RollupKind.SUM_BY_VALUE));
        assertThat(result[2], is(RollupKind.AVG_BY_VALUE));
        assertThat(result[3], is(RollupKind.NONE));
    }
}

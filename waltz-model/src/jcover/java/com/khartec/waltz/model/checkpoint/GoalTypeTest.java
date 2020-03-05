package com.khartec.waltz.model.checkpoint;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import org.junit.Test;

/**
 * Unit tests for com.khartec.waltz.model.checkpoint.GoalType
 *
 * @author Diffblue JCover
 */

public class GoalTypeTest {

    @Test
    public void valuesReturnsABOVE_THRESHOLDBELOW_THRESHOLDPERCENT_INCREASEPERCENT_DECREASE() {
        GoalType[] result = GoalType.values();
        assertThat(result[0], is(GoalType.ABOVE_THRESHOLD));
        assertThat(result[1], is(GoalType.BELOW_THRESHOLD));
        assertThat(result[2], is(GoalType.PERCENT_INCREASE));
        assertThat(result[3], is(GoalType.PERCENT_DECREASE));
    }
}

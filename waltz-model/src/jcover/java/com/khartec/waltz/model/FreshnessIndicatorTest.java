package com.khartec.waltz.model;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import org.junit.Test;

/**
 * Unit tests for com.khartec.waltz.model.FreshnessIndicator
 *
 * @author Diffblue JCover
 */

public class FreshnessIndicatorTest {

    @Test
    public void valuesReturnsNEVER_OBSERVEDHISTORICALLY_OBSERVEDOBSERVEDRECENTLY_OBSERVED() {
        FreshnessIndicator[] result = FreshnessIndicator.values();
        assertThat(result[0], is(FreshnessIndicator.NEVER_OBSERVED));
        assertThat(result[1], is(FreshnessIndicator.HISTORICALLY_OBSERVED));
        assertThat(result[2], is(FreshnessIndicator.OBSERVED));
        assertThat(result[3], is(FreshnessIndicator.RECENTLY_OBSERVED));
    }
}

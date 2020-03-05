package com.khartec.waltz.model;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import org.junit.Test;

/**
 * Unit tests for com.khartec.waltz.model.LifecycleStatus
 *
 * @author Diffblue JCover
 */

public class LifecycleStatusTest {

    @Test
    public void valuesReturnsACTIVEBUILDINGINACTIVERETIRINGUNKNOWN() {
        LifecycleStatus[] result = LifecycleStatus.values();
        assertThat(result[0], is(LifecycleStatus.ACTIVE));
        assertThat(result[1], is(LifecycleStatus.BUILDING));
        assertThat(result[2], is(LifecycleStatus.INACTIVE));
        assertThat(result[3], is(LifecycleStatus.RETIRING));
        assertThat(result[4], is(LifecycleStatus.UNKNOWN));
    }
}

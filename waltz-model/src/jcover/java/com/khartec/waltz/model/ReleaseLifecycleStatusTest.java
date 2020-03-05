package com.khartec.waltz.model;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import org.junit.Test;

/**
 * Unit tests for com.khartec.waltz.model.ReleaseLifecycleStatus
 *
 * @author Diffblue JCover
 */

public class ReleaseLifecycleStatusTest {

    @Test
    public void valuesReturnsDRAFTACTIVEDEPRECATEDOBSOLETE() {
        ReleaseLifecycleStatus[] result = ReleaseLifecycleStatus.values();
        assertThat(result[0], is(ReleaseLifecycleStatus.DRAFT));
        assertThat(result[1], is(ReleaseLifecycleStatus.ACTIVE));
        assertThat(result[2], is(ReleaseLifecycleStatus.DEPRECATED));
        assertThat(result[3], is(ReleaseLifecycleStatus.OBSOLETE));
    }
}

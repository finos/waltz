package com.khartec.waltz.model.application;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import org.junit.Test;

/**
 * Unit tests for com.khartec.waltz.model.application.LifecyclePhase
 *
 * @author Diffblue JCover
 */

public class LifecyclePhaseTest {

    @Test
    public void valuesReturnsPRODUCTIONDEVELOPMENTCONCEPTUALRETIRED() {
        LifecyclePhase[] result = LifecyclePhase.values();
        assertThat(result[0], is(LifecyclePhase.PRODUCTION));
        assertThat(result[1], is(LifecyclePhase.DEVELOPMENT));
        assertThat(result[2], is(LifecyclePhase.CONCEPTUAL));
        assertThat(result[3], is(LifecyclePhase.RETIRED));
    }
}

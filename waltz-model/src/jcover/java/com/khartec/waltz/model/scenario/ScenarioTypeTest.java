package com.khartec.waltz.model.scenario;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import org.junit.Test;

/**
 * Unit tests for com.khartec.waltz.model.scenario.ScenarioType
 *
 * @author Diffblue JCover
 */

public class ScenarioTypeTest {

    @Test
    public void valuesReturnsTARGETCURRENTINTERIM() {
        ScenarioType[] result = ScenarioType.values();
        assertThat(result[0], is(ScenarioType.TARGET));
        assertThat(result[1], is(ScenarioType.CURRENT));
        assertThat(result[2], is(ScenarioType.INTERIM));
    }
}

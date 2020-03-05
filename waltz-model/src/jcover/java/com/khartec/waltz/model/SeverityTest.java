package com.khartec.waltz.model;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import org.junit.Test;

/**
 * Unit tests for com.khartec.waltz.model.Severity
 *
 * @author Diffblue JCover
 */

public class SeverityTest {

    @Test
    public void valuesReturnsINFORMATIONWARNINGERROR() {
        Severity[] result = Severity.values();
        assertThat(result[0], is(Severity.INFORMATION));
        assertThat(result[1], is(Severity.WARNING));
        assertThat(result[2], is(Severity.ERROR));
    }
}

package com.khartec.waltz.model;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import org.junit.Test;

/**
 * Unit tests for com.khartec.waltz.model.FlowDirection
 *
 * @author Diffblue JCover
 */

public class FlowDirectionTest {

    @Test
    public void valuesReturnsINBOUNDOUTBOUNDINTRA() {
        FlowDirection[] result = FlowDirection.values();
        assertThat(result[0], is(FlowDirection.INBOUND));
        assertThat(result[1], is(FlowDirection.OUTBOUND));
        assertThat(result[2], is(FlowDirection.INTRA));
    }
}

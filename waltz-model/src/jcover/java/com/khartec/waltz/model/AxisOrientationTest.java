package com.khartec.waltz.model;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import org.junit.Test;

/**
 * Unit tests for com.khartec.waltz.model.AxisOrientation
 *
 * @author Diffblue JCover
 */

public class AxisOrientationTest {

    @Test
    public void valuesReturnsROWCOLUMN() {
        AxisOrientation[] result = AxisOrientation.values();
        assertThat(result[0], is(AxisOrientation.ROW));
        assertThat(result[1], is(AxisOrientation.COLUMN));
    }
}

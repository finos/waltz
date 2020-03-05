package com.khartec.waltz.model.software_catalog;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import org.junit.Test;

/**
 * Unit tests for com.khartec.waltz.model.software_catalog.MaturityStatus
 *
 * @author Diffblue JCover
 */

public class MaturityStatusTest {

    @Test
    public void valuesReturnsPLANNEDINVESTHOLDDISINVESTUNSUPPORTEDRESTRICTEDUNKNOWN() {
        MaturityStatus[] result = MaturityStatus.values();
        assertThat(result[0], is(MaturityStatus.PLANNED));
        assertThat(result[1], is(MaturityStatus.INVEST));
        assertThat(result[2], is(MaturityStatus.HOLD));
        assertThat(result[3], is(MaturityStatus.DISINVEST));
        assertThat(result[4], is(MaturityStatus.UNSUPPORTED));
        assertThat(result[5], is(MaturityStatus.RESTRICTED));
        assertThat(result[6], is(MaturityStatus.UNKNOWN));
    }
}

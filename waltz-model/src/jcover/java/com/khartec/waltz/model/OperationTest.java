package com.khartec.waltz.model;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import org.junit.Test;

/**
 * Unit tests for com.khartec.waltz.model.Operation
 *
 * @author Diffblue JCover
 */

public class OperationTest {

    @Test
    public void valuesReturnsADDATTESTREMOVEUPDATEUNKNOWN() {
        Operation[] result = Operation.values();
        assertThat(result[0], is(Operation.ADD));
        assertThat(result[1], is(Operation.ATTEST));
        assertThat(result[2], is(Operation.REMOVE));
        assertThat(result[3], is(Operation.UPDATE));
        assertThat(result[4], is(Operation.UNKNOWN));
    }
}

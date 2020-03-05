package com.khartec.waltz.model.orphan;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import org.junit.Test;

/**
 * Unit tests for com.khartec.waltz.model.orphan.OrphanSide
 *
 * @author Diffblue JCover
 */

public class OrphanSideTest {

    @Test
    public void valuesReturnsABBOTH() {
        OrphanSide[] result = OrphanSide.values();
        assertThat(result[0], is(OrphanSide.A));
        assertThat(result[1], is(OrphanSide.B));
        assertThat(result[2], is(OrphanSide.BOTH));
    }
}

package com.khartec.waltz.model;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import java.util.Date;

import org.junit.Test;

/**
 * Unit tests for com.khartec.waltz.model.EndOfLifeStatus
 *
 * @author Diffblue JCover
 */

public class EndOfLifeStatusTest {

    @Test
    public void calculateEndOfLifeStatus() {
        assertThat(EndOfLifeStatus.calculateEndOfLifeStatus(null), is(EndOfLifeStatus.NOT_END_OF_LIFE));
        assertThat(EndOfLifeStatus.calculateEndOfLifeStatus(new Date(1L)), is(EndOfLifeStatus.END_OF_LIFE));
    }

    @Test
    public void valuesReturnsEND_OF_LIFENOT_END_OF_LIFE() {
        EndOfLifeStatus[] result = EndOfLifeStatus.values();
        assertThat(result[0], is(EndOfLifeStatus.END_OF_LIFE));
        assertThat(result[1], is(EndOfLifeStatus.NOT_END_OF_LIFE));
    }
}

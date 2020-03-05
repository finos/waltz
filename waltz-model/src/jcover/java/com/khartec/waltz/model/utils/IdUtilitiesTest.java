package com.khartec.waltz.model.utils;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertArrayEquals;

import com.khartec.waltz.model.IdCommandResponse;

import java.util.LinkedList;

import org.junit.Test;

/**
 * Unit tests for com.khartec.waltz.model.utils.IdUtilities
 *
 * @author Diffblue JCover
 */

public class IdUtilitiesTest {

    @Test
    public void isEmpty() {
        assertThat(IdUtilities.<IdCommandResponse>indexById(new LinkedList<IdCommandResponse>()).isEmpty(), is(true));
        assertThat(IdUtilities.<IdCommandResponse>indexByOptId(new LinkedList<IdCommandResponse>()).isEmpty(), is(true));
    }

    @Test
    public void toIdArrayXsIsEmptyReturnsEmpty() {
        assertArrayEquals(new Long[] { }, IdUtilities.toIdArray(new LinkedList<IdCommandResponse>()));
    }
}

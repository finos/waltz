package com.khartec.waltz.common.exception;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import org.junit.Test;

/**
 * Unit tests for com.khartec.waltz.common.exception.UpdateFailedException
 *
 * @author Diffblue JCover
 */

public class UpdateFailedExceptionTest {

    @Test
    public void getCode() {
        assertThat(new UpdateFailedException("OX13QD", "jpg").getCode(), is("OX13QD"));
    }
}

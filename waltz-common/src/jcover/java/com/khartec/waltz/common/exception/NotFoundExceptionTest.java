package com.khartec.waltz.common.exception;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import org.junit.Test;

/**
 * Unit tests for com.khartec.waltz.common.exception.NotFoundException
 *
 * @author Diffblue JCover
 */

public class NotFoundExceptionTest {

    @Test
    public void getCode() {
        assertThat(new NotFoundException("OX13QD", "jpg").getCode(), is("OX13QD"));
    }
}

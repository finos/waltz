package com.khartec.waltz.common.exception;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.nullValue;

import org.junit.Test;

/**
 * Unit tests for com.khartec.waltz.common.exception.InsufficientPrivelegeException
 *
 * @author Diffblue JCover
 */

public class InsufficientPrivelegeExceptionTest {

    @Test
    public void constructor() {
        InsufficientPrivelegeException insufficientPrivelegeException = new InsufficientPrivelegeException("jpg");
        assertThat(insufficientPrivelegeException.getCause(), is(nullValue()));
        assertThat(insufficientPrivelegeException.getLocalizedMessage(), is("jpg"));
        assertThat(insufficientPrivelegeException.getMessage(), is("jpg"));
    }
}

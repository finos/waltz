package com.khartec.waltz.common.exception;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.nullValue;

import org.junit.Test;

/**
 * Unit tests for com.khartec.waltz.common.exception.DuplicateKeyException
 *
 * @author Diffblue JCover
 */

public class DuplicateKeyExceptionTest {

    @Test
    public void constructor() {
        DuplicateKeyException duplicateKeyException = new DuplicateKeyException("jpg");
        assertThat(duplicateKeyException.getCause(), is(nullValue()));
        assertThat(duplicateKeyException.getLocalizedMessage(), is("jpg"));
        assertThat(duplicateKeyException.getMessage(), is("jpg"));
    }
}

package com.khartec.waltz.model.exceptions;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.nullValue;

import org.junit.Test;

/**
 * Unit tests for com.khartec.waltz.model.exceptions.NotAuthorizedException
 *
 * @author Diffblue JCover
 */

public class NotAuthorizedExceptionTest {

    @Test
    public void constructor() {
        NotAuthorizedException notAuthorizedException = new NotAuthorizedException();
        assertThat(notAuthorizedException.getCause(), is(nullValue()));
        assertThat(notAuthorizedException.getLocalizedMessage(), is("User is not authorized to perform that action"));
        assertThat(notAuthorizedException.getMessage(), is("User is not authorized to perform that action"));
    }
}

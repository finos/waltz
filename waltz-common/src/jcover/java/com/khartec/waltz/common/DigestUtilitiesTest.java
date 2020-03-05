package com.khartec.waltz.common;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import org.junit.Test;

/**
 * Unit tests for com.khartec.waltz.common.DigestUtilities
 *
 * @author Diffblue JCover
 */

public class DigestUtilitiesTest {

    @Test
    public void digestBytesIsOne() throws java.security.NoSuchAlgorithmException {
        byte[] bytes = new byte[] { 1 };
        assertThat(DigestUtilities.digest(bytes), is("v4tFMNjSRt10rFOhNHG7oXlB3/c="));
    }
}

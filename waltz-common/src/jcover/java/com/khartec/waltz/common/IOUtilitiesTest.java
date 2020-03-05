package com.khartec.waltz.common;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import java.io.ByteArrayOutputStream;
import java.io.StringBufferInputStream;

import org.junit.Test;
import org.springframework.core.io.Resource;

/**
 * Unit tests for com.khartec.waltz.common.IOUtilities
 *
 * @author Diffblue JCover
 */

public class IOUtilitiesTest {

    @Test
    public void copyStream() throws java.io.IOException {
        IOUtilities.copyStream(new StringBufferInputStream("Broadway"), new ByteArrayOutputStream());
    }

    @Test
    public void readAsStringReturnsBroadway() {
        assertThat(IOUtilities.readAsString(new StringBufferInputStream("Broadway")), is("Broadway"));
    }

    @Test
    public void readLinesReturnsBroadway() {
        assertThat(IOUtilities.readLines(new StringBufferInputStream("Broadway")).size(), is(1));
        assertThat(IOUtilities.readLines(new StringBufferInputStream("Broadway")).get(0), is("Broadway"));
    }
}

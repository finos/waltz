package com.khartec.waltz.common;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.nullValue;

import java.util.function.Supplier;

import org.junit.Test;

/**
 * Unit tests for com.khartec.waltz.common.FunctionUtilities
 *
 * @author Diffblue JCover
 */

public class FunctionUtilitiesTest {

    @Test
    public void discardResult() {
        FunctionUtilities.discardResult(new Aliases());
    }

    @Test
    public void time() {
        FunctionUtilities.time("/bin/bash", Thread.currentThread());
    }

    @Test
    public void timeReturnsNull() {
        @SuppressWarnings("unchecked")
        Supplier<String> supplier = null;
        assertThat(FunctionUtilities.<String>time("/bin/bash", supplier), is(nullValue()));
    }
}

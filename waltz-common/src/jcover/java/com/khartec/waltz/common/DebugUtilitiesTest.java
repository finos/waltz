package com.khartec.waltz.common;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsSame.sameInstance;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

/**
 * Unit tests for com.khartec.waltz.common.DebugUtilities
 *
 * @author Diffblue JCover
 */

public class DebugUtilitiesTest {

    @Test
    public void dumpMIsEmptyReturnsEmpty() {
        Map<String, String> m = new HashMap<String, String>();
        Map<String, String> result = DebugUtilities.<String, String>dump(m);
        assertThat(result.isEmpty(), is(true));
        assertThat(m, sameInstance(result));
    }

    @Test
    public void logValueResultIsFooReturnsFoo() {
        assertThat(DebugUtilities.<String>logValue("foo", new Object[] { new Aliases() }), is("foo"));
    }
}

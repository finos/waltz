package com.khartec.waltz.common;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.nullValue;

import org.junit.Test;

/**
 * Unit tests for com.khartec.waltz.common.ObjectUtilities
 *
 * @author Diffblue JCover
 */

public class ObjectUtilitiesTest {

    @Test
    public void any() {
        assertThat(ObjectUtilities.<String>any(new RangeBand<String>("foo", "foo"), "bar"), is(false));
        assertThat(ObjectUtilities.<String>any(new RangeBand<String>("bar", "foo"), "foo"), is(true));
    }

    @Test
    public void dumpXIsFooReturnsFoo() {
        assertThat(ObjectUtilities.<String>dump("foo"), is("foo"));
    }

    @Test
    public void firstNotNull() {
        assertThat(ObjectUtilities.<String>firstNotNull("Anna"), is("Anna"));
        assertThat(ObjectUtilities.<String>firstNotNull(), is(nullValue()));
        assertThat(ObjectUtilities.<String>firstNotNull(new String[] { null }), is(nullValue()));
    }
}

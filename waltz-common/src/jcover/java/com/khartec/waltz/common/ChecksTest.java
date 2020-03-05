package com.khartec.waltz.common;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsSame.sameInstance;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.LinkedList;

import org.junit.Test;

/**
 * Unit tests for com.khartec.waltz.common.Checks
 *
 * @author Diffblue JCover
 */

public class ChecksTest {

    @Test
    public void checkAllTsIsEmptyReturnsEmpty() {
        Collection<String> ts = new LinkedList<String>();
        Collection<String> result = Checks.<String>checkAll(ts, new RangeBand<String>("bar", "foo"), "foo");
        assertTrue(result.isEmpty());
        assertThat(ts, sameInstance(result));
    }

    @Test
    public void checkAllTsIsFooReturnsFoo() {
        String[] ts = new String[] { "foo" };
        assertArrayEquals(new String[] { "foo" }, Checks.<String>checkAll(ts, new RangeBand<String>("bar", "foo"), "foo"));
    }

    @Test
    public void checkNotEmptyStrIsBarReturnsBar() {
        assertThat(Checks.checkNotEmpty("bar", "foo"), is("bar"));
    }

    @Test
    public void checkNotEmptyTsIsBar() {
        String[] ts = new String[] { "bar" };
        Checks.<String>checkNotEmpty(ts, "foo");
    }

    @Test
    public void checkNotEmptyTsIsFoo() {
        Collection<String> ts = new LinkedList<String>();
        ((LinkedList<String>)ts).add("foo");
        Checks.<String>checkNotEmpty(ts, "foo");
    }
}

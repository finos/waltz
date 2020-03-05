package com.khartec.waltz.common;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import java.util.Collection;
import java.util.LinkedList;

import org.junit.Test;

/**
 * Unit tests for com.khartec.waltz.common.PredicateUtilities
 *
 * @author Diffblue JCover
 */

public class PredicateUtilitiesTest {

    @Test
    public void allTsIsBarReturnsFalse1() {
        String[] ts = new String[] { "bar" };
        assertThat(PredicateUtilities.<String>all(ts, new RangeBand<String>("foo", "foo")), is(false));
    }

    @Test
    public void allTsIsBarReturnsFalse2() {
        Collection<String> ts = new LinkedList<String>();
        ((LinkedList<String>)ts).add("bar");
        assertThat(PredicateUtilities.<String>all(ts, new RangeBand<String>("foo", "foo")), is(false));
    }

    @Test
    public void allTsIsEmptyReturnsTrue() {
        assertThat(PredicateUtilities.<String>all(new LinkedList<String>(), new RangeBand<String>("bar", "foo")), is(true));
    }

    @Test
    public void allTsIsFooReturnsTrue() {
        String[] ts = new String[] { "foo" };
        assertThat(PredicateUtilities.<String>all(ts, new RangeBand<String>("bar", "foo")), is(true));
    }

    @Test
    public void anyTsIsBarReturnsFalse() {
        String[] ts = new String[] { "bar" };
        assertThat(PredicateUtilities.<String>any(ts, new RangeBand<String>("foo", "foo")), is(false));
    }

    @Test
    public void anyTsIsEmptyReturnsFalse() {
        assertThat(PredicateUtilities.<String>any(new LinkedList<String>(), new RangeBand<String>("bar", "foo")), is(false));
    }

    @Test
    public void anyTsIsFooReturnsTrue1() {
        Collection<String> ts = new LinkedList<String>();
        ((LinkedList<String>)ts).add("foo");
        assertThat(PredicateUtilities.<String>any(ts, new RangeBand<String>("foo", "foo")), is(true));
    }

    @Test
    public void anyTsIsFooReturnsTrue2() {
        String[] ts = new String[] { "foo" };
        assertThat(PredicateUtilities.<String>any(ts, new RangeBand<String>("bar", "foo")), is(true));
    }

    @Test
    public void noneTsIsBarReturnsTrue() {
        String[] ts = new String[] { "bar" };
        assertThat(PredicateUtilities.<String>none(ts, new RangeBand<String>("foo", "foo")), is(true));
    }

    @Test
    public void noneTsIsEmptyReturnsTrue() {
        assertThat(PredicateUtilities.<String>none(new LinkedList<String>(), new RangeBand<String>("bar", "foo")), is(true));
    }

    @Test
    public void noneTsIsFooReturnsFalse1() {
        Collection<String> ts = new LinkedList<String>();
        ((LinkedList<String>)ts).add("foo");
        assertThat(PredicateUtilities.<String>none(ts, new RangeBand<String>("foo", "foo")), is(false));
    }

    @Test
    public void noneTsIsFooReturnsFalse2() {
        String[] ts = new String[] { "foo" };
        assertThat(PredicateUtilities.<String>none(ts, new RangeBand<String>("bar", "foo")), is(false));
    }

    @Test
    public void testReturnsTrue() {
        assertThat(PredicateUtilities.<String>not(new RangeBand<String>("foo", "foo")).test("html"), is(true));
    }
}

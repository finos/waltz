package com.khartec.waltz.common;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.LinkedList;
import java.util.function.Consumer;
import java.util.function.Function;

import org.junit.Test;

/**
 * Unit tests for com.khartec.waltz.common.CollectionUtilities
 *
 * @author Diffblue JCover
 */

public class CollectionUtilitiesTest {

    @Test
    public void anyTsIsEmptyReturnsFalse() {
        assertThat(CollectionUtilities.<String>any(new LinkedList<String>(), new RangeBand<String>("bar", "foo")), is(false));
    }

    @Test
    public void anyTsIsFooReturnsTrue() {
        Collection<String> ts = new LinkedList<String>();
        ((LinkedList<String>)ts).add("foo");
        assertThat(CollectionUtilities.<String>any(ts, new RangeBand<String>("foo", "foo")), is(true));
    }

    @Test
    public void filterXsIsEmptyReturnsEmpty() {
        assertTrue(CollectionUtilities.<String>filter(new LinkedList<String>(), new RangeBand<String>("foo", "foo")).isEmpty());
    }

    @Test
    public void headXsIsFooReturnsFoo() {
        Collection<String> xs = new LinkedList<String>();
        ((LinkedList<String>)xs).add("foo");
        assertThat(CollectionUtilities.<String>head(xs).get(), is("foo"));
    }

    @Test
    public void isEmptyXsIsEmptyReturnsTrue() {
        assertThat(CollectionUtilities.<String>isEmpty(new LinkedList<String>()), is(true));
    }

    @Test
    public void isEmptyXsIsFooReturnsFalse() {
        Collection<String> xs = new LinkedList<String>();
        ((LinkedList<String>)xs).add("foo");
        assertThat(CollectionUtilities.<String>isEmpty(xs), is(false));
    }

    @Test
    public void isPresent() {
        assertThat(CollectionUtilities.<String>head(new LinkedList<String>()).isPresent(), is(false));
        assertThat(CollectionUtilities.<String>head(null).isPresent(), is(false));
        assertThat(CollectionUtilities.<String>maybeFirst(new LinkedList<String>()).isPresent(), is(false));
        assertThat(CollectionUtilities.<String>maybeFirst(new LinkedList<String>(), new RangeBand<String>("foo", "foo")).isPresent(), is(false));
    }

    @Test
    public void maybe() {
        @SuppressWarnings("unchecked")
        Consumer<Collection<String>> fn = null;
        CollectionUtilities.<String>maybe(new LinkedList<String>(), fn);
    }

    @Test
    public void maybeDfltIsFooReturnsFoo() {
        @SuppressWarnings("unchecked")
        Function<Collection<String>, String> fn = null;
        assertThat(CollectionUtilities.<String, String>maybe(new LinkedList<String>(), fn, "foo"), is("foo"));
    }

    @Test
    public void maybeFirstXsIsAnnaReturnsAnna() {
        Collection<String> xs = new LinkedList<String>();
        ((LinkedList<String>)xs).add("Anna");
        assertThat(CollectionUtilities.<String>maybeFirst(xs).get(), is("Anna"));
    }

    @Test
    public void notEmpty() {
        assertThat(CollectionUtilities.<String>notEmpty(new LinkedList<String>()), is(false));
        assertThat(CollectionUtilities.<String>notEmpty(null), is(false));
    }

    @Test
    public void notEmptyTsIsFooReturnsTrue() {
        Collection<String> ts = new LinkedList<String>();
        ((LinkedList<String>)ts).add("foo");
        assertThat(CollectionUtilities.<String>notEmpty(ts), is(true));
    }

    @Test
    public void sumIntsValuesIsEmptyReturnsZero() {
        assertThat(CollectionUtilities.sumInts(new LinkedList<Integer>()), is(0L));
    }

    @Test
    public void sumIntsValuesIsOneReturnsOne() {
        Collection<Integer> values = new LinkedList<Integer>();
        ((LinkedList<Integer>)values).add(1);
        assertThat(CollectionUtilities.sumInts(values), is(1L));
    }

    @Test
    public void sumLongsValuesIsEmptyReturnsZero() {
        assertThat(CollectionUtilities.sumLongs(new LinkedList<Long>()), is(0L));
    }

    @Test
    public void sumLongsValuesIsOneReturnsOne() {
        Collection<Long> values = new LinkedList<Long>();
        ((LinkedList<Long>)values).add(1L);
        assertThat(CollectionUtilities.sumLongs(values), is(1L));
    }
}

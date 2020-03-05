package com.khartec.waltz.common;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

import org.junit.Test;

/**
 * Unit tests for com.khartec.waltz.common.ListUtilities
 *
 * @author Diffblue JCover
 */

public class ListUtilitiesTest {

    @Test
    public void appendTIsSmithAndTsIsEmptyReturnsSmith() {
        assertThat(ListUtilities.<String>append(new ArrayList<String>(), "Smith").size(), is(1));
        assertThat(ListUtilities.<String>append(new ArrayList<String>(), "Smith").get(0), is("Smith"));
    }

    @Test
    public void applyToFirstXsIsEmpty() {
        @SuppressWarnings("unchecked")
        Function<String, String> mapFn = null;
        assertThat(ListUtilities.<String, String>applyToFirst(new ArrayList<String>(), mapFn).isPresent(), is(false));
    }

    @Test
    public void asListTsIsSmithReturnsSmith() {
        assertThat(ListUtilities.<String>asList("Smith").size(), is(1));
        assertThat(ListUtilities.<String>asList("Smith").get(0), is("Smith"));
    }

    @Test
    public void compactTsIsEmptyReturnsEmpty() {
        assertTrue(ListUtilities.<String>compact(new LinkedList<String>()).isEmpty());
    }

    @Test
    public void dropCountIsOneAndTsIsEmptyReturnsEmpty() {
        assertTrue(ListUtilities.<String>drop(new ArrayList<String>(), 1).isEmpty());
    }

    @Test
    public void ensureNotNull() {
        assertTrue(ListUtilities.<String>ensureNotNull(new LinkedList<String>()).isEmpty());
        assertTrue(ListUtilities.<String>ensureNotNull(null).isEmpty());
    }

    @Test
    public void filterTsIsEmptyReturnsEmpty() {
        assertTrue(ListUtilities.<String>filter(new RangeBand<String>("foo", "foo"), new ArrayList<String>()).isEmpty());
    }

    @Test
    public void isEmpty() {
        assertThat(ListUtilities.<String>isEmpty(new ArrayList<String>()), is(true));
        assertThat(ListUtilities.<String>isEmpty(null), is(true));
    }

    @Test
    public void isEmptyTsIsSmithReturnsFalse() {
        List<String> ts = new ArrayList<String>();
        ((ArrayList<String>)ts).add("Smith");
        assertThat(ListUtilities.<String>isEmpty(ts), is(false));
    }

    @Test
    public void push() {
        assertThat(ListUtilities.<String>push(new ArrayList<String>(), "Smith").size(), is(1));
        assertThat(ListUtilities.<String>push(new ArrayList<String>(), "Smith").get(0), is("Smith"));
        assertThat(ListUtilities.<String>push(null, "Smith").size(), is(1));
        assertThat(ListUtilities.<String>push(null, "Smith").get(0), is("Smith"));
    }

    @Test
    public void reverseTsIsEmptyReturnsEmpty() {
        assertTrue(ListUtilities.<String>reverse(new ArrayList<String>()).isEmpty());
    }
}

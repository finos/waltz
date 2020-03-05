package com.khartec.waltz.common;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.function.Function;

import org.junit.Test;

/**
 * Unit tests for com.khartec.waltz.common.SetUtilities
 *
 * @author Diffblue JCover
 */

public class SetUtilitiesTest {

    @Test
    public void asSet() {
        assertTrue(SetUtilities.<String>asSet().isEmpty());
        assertThat(SetUtilities.<String>asSet("foo").size(), is(1));
        assertTrue(SetUtilities.<String>asSet("foo").contains("foo"));
        assertTrue(SetUtilities.<String>asSet(null).isEmpty());
    }

    @Test
    public void fromCollection() {
        assertTrue(SetUtilities.<String>fromCollection(new LinkedList<String>()).isEmpty());
        assertTrue(SetUtilities.<String>fromCollection(null).isEmpty());
    }

    @Test
    public void fromCollectionTsIsFooReturnsFoo() {
        Collection<String> ts = new LinkedList<String>();
        ((LinkedList<String>)ts).add("foo");
        assertThat(SetUtilities.<String>fromCollection(ts).size(), is(1));
        assertTrue(SetUtilities.<String>fromCollection(ts).contains("foo"));
    }

    @Test
    public void intersectionXsIsEmptyAndYsIsEmptyReturnsEmpty() {
        assertTrue(SetUtilities.<String>intersection(new HashSet<String>(), new HashSet<String>()).isEmpty());
    }

    @Test
    public void mapXsIsEmpty() {
        @SuppressWarnings("unchecked")
        Function<String, String> fn = null;
        assertTrue(SetUtilities.<String, String>map(new LinkedList<String>(), fn).isEmpty());
    }

    @Test
    public void mapXsIsNull() {
        @SuppressWarnings("unchecked")
        Function<String, String> fn = null;
        assertTrue(SetUtilities.<String, String>map(null, fn).isEmpty());
    }

    @Test
    public void minusXsIsEmptyReturnsEmpty() {
        assertTrue(SetUtilities.<String>minus(new HashSet<String>(), new HashSet<String>()).isEmpty());
    }

    @Test
    public void orderedUnionReturnsEmpty() {
        assertTrue(SetUtilities.<String>orderedUnion(new LinkedList<String>()).isEmpty());
    }

    @Test
    public void unionAllValuesIsEmptyReturnsEmpty() {
        assertTrue(SetUtilities.<String>unionAll(new LinkedList<LinkedList<String>>()).isEmpty());
    }

    @Test
    public void unionReturnsEmpty() {
        assertTrue(SetUtilities.<String>union(new LinkedList<String>()).isEmpty());
    }

    @Test
    public void uniqByXsIsEmptyReturnsEmpty() {
        @SuppressWarnings("unchecked")
        Function<String, String> comparator = null;
        assertTrue(SetUtilities.<String, String>uniqBy(new LinkedList<String>(), comparator).isEmpty());
    }
}

package com.khartec.waltz.common;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsSame.sameInstance;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.function.Function;

import org.junit.Test;

/**
 * Unit tests for com.khartec.waltz.common.MapUtilities
 *
 * @author Diffblue JCover
 */

public class MapUtilitiesTest {

    @Test
    public void countByXsIsEmpty() {
        @SuppressWarnings("unchecked")
        Function<String, String> keyFn = null;
        assertThat(MapUtilities.<String, String>countBy(keyFn, new LinkedList<String>()).isEmpty(), is(true));
    }

    @Test
    public void countByXsIsNull() {
        @SuppressWarnings("unchecked")
        Function<String, String> keyFn = null;
        assertThat(MapUtilities.<String, String>countBy(keyFn, null).isEmpty(), is(true));
    }

    @Test
    public void ensureNotNullMaybeMapIsEmpty() {
        Map<String, String> maybeMap = new HashMap<String, String>();
        Map<String, String> result = MapUtilities.<String, String>ensureNotNull(maybeMap);
        assertThat(result.isEmpty(), is(true));
        assertThat(maybeMap, sameInstance(result));
    }

    @Test
    public void get() {
        assertThat(MapUtilities.<String, String>newHashMap("foo", "foo", "foo", "foo").get("foo"), is("foo"));
        assertThat(MapUtilities.<String, String>newHashMap("foo", "foo", "foo", "foo", "foo", "foo").get("foo"), is("foo"));
        assertThat(MapUtilities.<String, String>newHashMap("foo", "foo", "foo", "foo", "foo", "foo", "foo", "foo", "foo", "foo", "foo", "foo").get("foo"), is("foo"));
        assertThat(MapUtilities.<String, String>newHashMap("foo", "foo", "foo", "foo", "foo", "foo", "foo", "foo", "foo", "foo").get("foo"), is("foo"));
        assertThat(MapUtilities.<String, String>newHashMap("foo", "foo", "foo", "foo", "foo", "foo", "foo", "foo").get("foo"), is("foo"));
        assertThat(MapUtilities.<String, String>newHashMap("foo", "foo").get("foo"), is("foo"));
    }

    @Test
    public void isEmpty() {
        assertThat(MapUtilities.<String, String>ensureNotNull(null).isEmpty(), is(true));
        assertThat(MapUtilities.<String, String>isEmpty(new HashMap<String, String>()), is(true));
        assertThat(MapUtilities.<String, String>isEmpty(null), is(true));
        assertThat(MapUtilities.<String, String>newHashMap().isEmpty(), is(true));
    }

    @Test
    public void isEmptyMapIsFooReturnsFalse() {
        Map<String, String> map = new HashMap<String, String>();
        ((HashMap<String, String>)map).put("foo", "foo");
        assertThat(MapUtilities.<String, String>isEmpty(map), is(false));
    }

    @Test
    public void isPresent() {
        assertThat(MapUtilities.<String, String>maybeGet(new HashMap<String, String>(), "foo").isPresent(), is(false));
        assertThat(MapUtilities.<String, String>maybeGet(null, "foo").isPresent(), is(false));
    }

    @Test
    public void transformKeysOriginalIsEmptyReturnsEmpty() {
        @SuppressWarnings("unchecked")
        Function<String, String> transformation = null;
        assertThat(MapUtilities.<String, String, String>transformKeys(new HashMap<String, String>(), transformation).isEmpty(), is(true));
    }
}

package com.khartec.waltz.common;

import org.junit.Test;

import java.util.Map;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class MapUtilities_maybeGet {

    @Test
    public void simpleMaybeGet() {
        Map<Integer, String> xs = MapUtilities.newHashMap(1, "aa");
        Optional<String> result = MapUtilities.maybeGet( xs, 1 );
        assertEquals("aa", result.get());
    }

    @Test
    public void simpleMaybeGetWithNullMap() {
        Map<Integer, String> xs = null;
        Optional<String> result = MapUtilities.maybeGet( xs, 1 );
        assertFalse(result.isPresent());
    }

    @Test
    public void simpleMaybeGetWithElementNotFound() {
        Map<Integer, String> xs = MapUtilities.newHashMap(1, "aa");;
        Optional<String> result = MapUtilities.maybeGet( xs, 2 );
        assertFalse(result.isPresent());
    }
}

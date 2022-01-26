package org.finos.waltz.common;

import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class MapUtilities_maybeGetTest {

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

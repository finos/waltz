package org.finos.waltz.common;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class StreamUtilities_tapTest {

    @Test
    public void simpleTap(){
        Set<String> tapped = new HashSet<>();
        Function<String, String> tapper = StreamUtilities.tap(tapped::add);

        Stream.of("hello", "world").map(tapper).collect(Collectors.toList());

        assertEquals(2, tapped.size());
        assertTrue(tapped.contains("hello"));
        assertTrue(tapped.contains("world"));
    }
}

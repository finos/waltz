package org.finos.waltz.common;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.finos.waltz.common.StreamUtilities.mkSiphon;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class StreamUtilities_siphonTest {

    @Test
    public void siphon(){
        StreamUtilities.Siphon<String> siphon = mkSiphon(x -> x.startsWith("h"));

        List<String> result = Stream
                .of("hello", "world", "happy", "earth", "hip", "hip", "hooray")
                .filter(siphon)
                .collect(Collectors.toList());

        assertEquals(ListUtilities.asList("world", "earth"), result);
        assertTrue(siphon.hasResults());
        assertEquals(ListUtilities.asList("hello", "happy", "hip", "hip", "hooray"), siphon.getResults());

    }
}

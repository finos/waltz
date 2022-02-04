package org.finos.waltz.common;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class OptionalUtilities_maybeTest {
    @Test
    public void simpleMaybe(){
        Optional<String> result = OptionalUtilities.maybe("a");
        assertEquals(Optional.of("a"), result);
    }

    @Test
    public void simpleMaybeWithEmptyVal(){
        Optional<String> result = OptionalUtilities.maybe("");
        assertEquals(Optional.of(""), result);
    }

    @Test
    public void simpleMaybeWithNullVal(){
        Optional<String> result = OptionalUtilities.maybe(null);
        assertEquals(Optional.empty(), result);
    }
}

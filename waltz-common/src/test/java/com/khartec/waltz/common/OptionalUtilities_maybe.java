package com.khartec.waltz.common;

import org.junit.Test;

import java.util.Optional;

import static org.junit.Assert.assertEquals;

public class OptionalUtilities_maybe {
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

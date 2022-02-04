package org.finos.waltz.common;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class OptionalUtilities_contentsEqualTest {
    @Test
    public void simpleContentsEqualWhenEq(){
        Optional<String> ele1 = Optional.of("a");
        boolean result = OptionalUtilities.contentsEqual(ele1,"a");
        assertTrue(result);
    }

    @Test
    public void simpleContentsEqualWhenNotEq(){
        Optional<String> ele1 = Optional.of("a");
        boolean result = OptionalUtilities.contentsEqual(ele1,"b");
        assertFalse(result);
    }

    @Test
    public void simpleContentsEqualWhenNullVal(){
        Optional<String> ele1 = Optional.of("a");
        boolean result = OptionalUtilities.contentsEqual(ele1,null);
        assertFalse(result);
    }

    @Test
    public void simpleContentsEqualWhenAllNull() {
        assertThrows(IllegalArgumentException.class,
                () -> OptionalUtilities.contentsEqual(null, null));
    }
}

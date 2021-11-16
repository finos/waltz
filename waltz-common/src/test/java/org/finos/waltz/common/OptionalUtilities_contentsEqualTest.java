package org.finos.waltz.common;

import org.junit.Test;

import java.util.Optional;

import static org.junit.Assert.assertEquals;

public class OptionalUtilities_contentsEqualTest {
    @Test
    public void simpleContentsEqualWhenEq(){
        Optional<String> ele1 = Optional.of("a");
        boolean result = OptionalUtilities.contentsEqual(ele1,"a");
        assertEquals(true, result);
    }

    @Test
    public void simpleContentsEqualWhenNotEq(){
        Optional<String> ele1 = Optional.of("a");
        boolean result = OptionalUtilities.contentsEqual(ele1,"b");
        assertEquals(false, result);
    }

    @Test
    public void simpleContentsEqualWhenNullVal(){
        Optional<String> ele1 = Optional.of("a");
        boolean result = OptionalUtilities.contentsEqual(ele1,null);
        assertEquals(false, result);
    }

    @Test(expected = IllegalArgumentException.class)
    public void simpleContentsEqualWhenAllNull(){
        OptionalUtilities.contentsEqual(null,null);
    }
}

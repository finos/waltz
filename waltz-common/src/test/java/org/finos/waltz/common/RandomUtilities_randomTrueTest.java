package org.finos.waltz.common;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RandomUtilities_randomTrueTest {
    @Test
    public void simpleRandomTrueWithOne(){
        boolean result = RandomUtilities.randomTrue(1.0);
        assertTrue(result);
    }

    @Test
    public void simpleRandomTrueWithZero(){
        boolean result = RandomUtilities.randomTrue(0.0);
        assertFalse(result);
    }

    @Test
    public void simpleRandomTrueWithNegOne(){
        boolean result = RandomUtilities.randomTrue(-1.0);
        assertFalse(result);
    }

    @Test
    public void simpleRandomTrueWithNegZero(){
        boolean result = RandomUtilities.randomTrue(-0.0);
        assertFalse(result);
    }

}

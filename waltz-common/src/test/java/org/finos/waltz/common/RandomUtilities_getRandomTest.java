package org.finos.waltz.common;

import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.finos.waltz.common.RandomUtilities.pickAndRemove;
import static org.junit.jupiter.api.Assertions.*;

public class RandomUtilities_getRandomTest {
    @Test
    public void simpleGetRandomForOne(){
        Random result = RandomUtilities.getRandom();
        assertEquals(0, result.nextInt(1));
    }

    @Test
    public void simpleGetRandomForZero() {
        Random result = RandomUtilities.getRandom();

        assertThrows(IllegalArgumentException.class,
                () -> result.nextInt(0));
    }
}

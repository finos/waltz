package com.khartec.waltz.common;

import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class RandomUtilities_getRandom {
    @Test
    public void simpleGetRandomForOne(){
        Random result = RandomUtilities.getRandom();
        assertEquals(0, result.nextInt(1));
    }

    @Test(expected = IllegalArgumentException.class)
    public void simpleGetRandomForZero(){
        Random result = RandomUtilities.getRandom();
        result.nextInt(0);
    }
}

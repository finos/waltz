package com.khartec.waltz.common;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class RandomUtilities_randomIntBetween {
    @Test
    public void simpleRandomIntBetween(){
        int val = RandomUtilities.randomIntBetween(0,1);
        assertTrue(val<1);
    }
}

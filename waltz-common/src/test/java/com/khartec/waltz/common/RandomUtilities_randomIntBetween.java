package com.khartec.waltz.common;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class RandomUtilities_randomIntBetween {
    @Test
    public void simpleRandomIntBetween(){
        int val = RandomUtilities.randomIntBetween(0,1);
        assertTrue(val<1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void toggleRandomIntBetween(){
        RandomUtilities.randomIntBetween(1,0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void randomIntBetweenAllZero(){
        RandomUtilities.randomIntBetween(0,0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void randomIntBetweenAllOne(){
        RandomUtilities.randomIntBetween(1,1);
    }
}

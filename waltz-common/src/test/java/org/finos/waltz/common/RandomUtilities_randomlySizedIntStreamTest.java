package org.finos.waltz.common;

import org.junit.Test;

import java.util.stream.IntStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class RandomUtilities_randomlySizedIntStreamTest {
    @Test
    public void simpleRandomlySizedIntStream1(){
        IntStream val = RandomUtilities.randomlySizedIntStream(0,1);
        assertEquals(0, val.count());
    }

    @Test
    public void simpleRandomlySizedIntStream2(){
        IntStream val = RandomUtilities.randomlySizedIntStream(1,2);
        assertEquals(1, val.count());
    }

    @Test(expected = IllegalArgumentException.class)
    public void toggleRandomlySizedIntStream(){
        RandomUtilities.randomlySizedIntStream(1,0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void randomlySizedIntStreamAllZero(){
        RandomUtilities.randomlySizedIntStream(0,0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void randomlySizedIntStreamAllOne(){
        RandomUtilities.randomlySizedIntStream(1,1);
    }
}

package org.finos.waltz.common;

import org.junit.jupiter.api.Test;

import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

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

    @Test
    public void toggleRandomlySizedIntStream() {
        assertThrows(IllegalArgumentException.class,
                () -> RandomUtilities.randomlySizedIntStream(1, 0));
    }

    @Test
    public void randomlySizedIntStreamAllZero() {

        assertThrows(IllegalArgumentException.class,
                () -> RandomUtilities.randomlySizedIntStream(0, 0));
    }

    @Test
    public void randomlySizedIntStreamAllOne() {

        assertThrows(IllegalArgumentException.class,
                () -> RandomUtilities.randomlySizedIntStream(1, 1));
    }
}

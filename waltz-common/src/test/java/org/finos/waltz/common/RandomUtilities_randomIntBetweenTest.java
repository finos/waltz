package org.finos.waltz.common;

import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RandomUtilities_randomIntBetweenTest {
    @Test
    public void simpleRandomIntBetween(){
        int val = RandomUtilities.randomIntBetween(0,1);
        assertTrue(val<1);
    }

    @Test
    public void toggleRandomIntBetween() {
        assertThrows(IllegalArgumentException.class,
                () -> RandomUtilities.randomIntBetween(1, 0));
    }

    @Test
    public void randomIntBetweenAllZero() {

        assertThrows(IllegalArgumentException.class,
                () -> RandomUtilities.randomIntBetween(0, 0));
    }

    @Test
    public void randomIntBetweenAllOne() {

        assertThrows(IllegalArgumentException.class,
                () -> RandomUtilities.randomIntBetween(1, 1));
    }
}

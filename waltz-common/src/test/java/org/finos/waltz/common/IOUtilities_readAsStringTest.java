package org.finos.waltz.common;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class IOUtilities_readAsStringTest {
    @Test
    public void canReadLinesAsString() throws IOException {
        String str = IOUtilities.readAsString(getStream("lines.txt"));
        assertEquals(141, str.length());
    }


    @Test
    public void emptyGivesEmptyList() throws IOException {
        String str = IOUtilities.readAsString(getStream("empty.txt"));
        assertEquals(0, str.length());
    }


    @Test
    public void nullStreamThrowsException() throws IOException {
        assertThrows(IllegalArgumentException.class,
                () -> IOUtilities.readAsString(null));
    }


    private InputStream getStream(String name) {
        return this.getClass().getClassLoader().getResourceAsStream(name);
    }
}

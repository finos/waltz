package org.finos.waltz.common;

import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;

public class IOUtilities_streamLines {
    @Test
    public void canStreamLines() throws IOException {
        Stream<String> lines = IOUtilities.streamLines(getStream("lines.txt"));
        assertEquals(4, lines.count());
    }


    @Test
    public void emptyGivesEmptyList() throws IOException {
        Stream<String> lines = IOUtilities.streamLines(getStream("empty.txt"));
        assertEquals(0, lines.count());
    }


    @Test(expected = IllegalArgumentException.class)
    public void nullStreamThrowsException() throws IOException {
        IOUtilities.streamLines(null);
    }


    private InputStream getStream(String name) {
        return this.getClass().getClassLoader().getResourceAsStream(name);
    }
}

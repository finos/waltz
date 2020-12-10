package com.khartec.waltz.common;

import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class IOUtilities_readAsString {
    @Test
    public void canReadLinesAsString() throws IOException {
        String lines = IOUtilities.readAsString(getStream("lines.txt"));
        assertEquals(138, lines.length());
    }


    @Test
    public void emptyGivesEmptyList() throws IOException {
        String lines = IOUtilities.readAsString(getStream("empty.txt"));
        assertEquals(0, lines.length());
    }


    @Test(expected = IllegalArgumentException.class)
    public void nullStreamThrowsException() throws IOException {
        IOUtilities.readAsString(null);
    }


    private InputStream getStream(String name) {
        return this.getClass().getClassLoader().getResourceAsStream(name);
    }
}

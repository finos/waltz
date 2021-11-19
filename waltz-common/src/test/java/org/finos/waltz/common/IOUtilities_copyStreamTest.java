package org.finos.waltz.common;

import org.junit.Test;

import java.io.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class IOUtilities_copyStreamTest {
    @Test
    public void canStreamLines() throws IOException {
        IOUtilities.copyStream(getInputStream("lines.txt"), getOutputStream());
    }


    @Test
    public void emptyGivesEmptyList() throws IOException {
        IOUtilities.copyStream(getInputStream("empty.txt"), getOutputStream());
    }


    @Test(expected = IllegalArgumentException.class)
    public void nullInputStreamThrowsException() throws IOException {
        IOUtilities.copyStream(null, getOutputStream());
    }

    @Test
    public void nullOutputStreamThrowsException() throws IOException {
        IOUtilities.copyStream(getInputStream("empty.txt"), null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void nullStreamsThrowsException() throws IOException {
        IOUtilities.copyStream(null, null);
    }


    private InputStream getInputStream(String name) {
        return this.getClass().getClassLoader().getResourceAsStream(name);
    }

    private OutputStream getOutputStream() throws FileNotFoundException {
        OutputStream output = new FileOutputStream("output.txt");
        return output;
    }
}

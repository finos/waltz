package org.finos.waltz.common;

import org.junit.jupiter.api.Test;

import java.io.*;

import static org.finos.waltz.common.EnumUtilities.parseEnumWithAliases;
import static org.junit.jupiter.api.Assertions.*;

public class IOUtilities_copyStreamTest {
    @Test
    public void canStreamLines() throws IOException {
        IOUtilities.copyStream(getInputStream("lines.txt"), getOutputStream());
    }


    @Test
    public void emptyGivesEmptyList() throws IOException {
        IOUtilities.copyStream(getInputStream("empty.txt"), getOutputStream());
    }


    @Test
    public void nullInputStreamThrowsException() throws IOException {
        assertThrows(IllegalArgumentException.class,
                () -> IOUtilities.copyStream(null, getOutputStream()));
    }

    @Test
    public void nullOutputStreamThrowsException() throws IOException {
        assertThrows(IllegalArgumentException.class,
                () -> IOUtilities.copyStream(getInputStream("empty.txt"), null));
    }

    @Test
    public void nullStreamsThrowsException() throws IOException {
        assertThrows(IllegalArgumentException.class,
                () -> IOUtilities.copyStream(null, null));
    }


    private InputStream getInputStream(String name) {
        return this.getClass().getClassLoader().getResourceAsStream(name);
    }

    private OutputStream getOutputStream() throws FileNotFoundException {
        return new FileOutputStream("output.txt");
    }
}

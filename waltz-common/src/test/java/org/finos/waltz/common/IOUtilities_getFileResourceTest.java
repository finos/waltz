package org.finos.waltz.common;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.Resource;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

public class IOUtilities_getFileResourceTest {
    @Test
    public void canGetFileResource() throws IOException {
        Resource file = IOUtilities.getFileResource("lines.txt");
        assertTrue(file.exists());
        assertNotEquals(0, file.contentLength());
    }


    @Test
    public void canGetEmptyFileResource() throws IOException {
        Resource file = IOUtilities.getFileResource("empty.txt");
        assertTrue(file.exists());
        assertEquals(0, file.contentLength());
    }

    @Test
    public void cannotGetNonExistingFile() throws IOException {
        Resource file = IOUtilities.getFileResource("myFile.txt");
        assertFalse(file.exists());
    }


    @Test
    public void nullNameThrowsException() throws IOException {
        assertThrows(IllegalArgumentException.class,
                () -> IOUtilities.getFileResource(null));
    }
}

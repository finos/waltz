package org.finos.waltz.common;

import org.junit.Test;

import java.security.NoSuchAlgorithmException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class DigestUtilities_digestTest {
    @Test(expected = NullPointerException.class)
    public void digestNull() throws NoSuchAlgorithmException {
        DigestUtilities.digest(null);
    }

    @Test
    public void digestSingleByte() throws NoSuchAlgorithmException {
        byte[] b = {0};
        String myHash = "W6k8nbDP+T9StSHXQg5D9u2ieE8=";
        String result = DigestUtilities.digest(b);
        assertNotNull(result);
        assertEquals( myHash, result);
    }

    @Test
    public void digestMultipleBytes() throws NoSuchAlgorithmException{
        byte[] b = {0,1,1};
        String myHash = "7uRH7cef6hynx9NORjJhzaS6M54=";
        String result = DigestUtilities.digest(b);
        assertNotNull(result);
        assertEquals(myHash, result);
    }
}

package org.finos.waltz.common;

import org.junit.jupiter.api.Test;

import static org.finos.waltz.common.StringUtilities.sanitizeCharacters;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class StringUtilities_sanitizeCharactersTest {

    @Test
    public void returnsNullForNullString() {
        String str = null;
        assertNull(sanitizeCharacters(str), "Should return null if null string provided");
    }

    @Test
    public void replacesDodgyHyphenCharacter() {
        String str = "Hello‐There!";
        String expectedString = "Hello-There!";
        assertEquals(expectedString, sanitizeCharacters(str));
    }

    @Test
    public void replacesDodgyBulletPointCharacterUnicode() {
        String str = "Test list:\n" +
                "\n" +
                "\uF0D8\tone\n" +
                "\uF0D8\ttwo\n";

        String expectedString = "Test list:\n" +
                "\n" +
                "-\tone\n" +
                "-\ttwo\n";

        assertEquals(expectedString, sanitizeCharacters(str));

    }

    @Test
    public void replacesDodgyBulletPointCharacterString() {
        String str = "Test list:\n" +
                "\n" +
                "\tone\n" +
                "\ttwo\n";

        String expectedString = "Test list:\n" +
                "\n" +
                "-\tone\n" +
                "-\ttwo\n";


        assertEquals(expectedString, sanitizeCharacters(str));
    }

    @Test
    public void replacesZeroWidthSpace() {
        String str = "​Test list:\n" +
                "\n" +
                "one​\n" +
                "two\n";

        String expectedString = "Test list:\n" +
                "\n" +
                "one\n" +
                "two\n";


        assertEquals(expectedString, sanitizeCharacters(str));
    }

    @Test
    public void replacesMultipleDodgyCharacters() {
        String str = "​Test list:\n" +
                "\n" +
                "â€one“​\n" +
                "â€™two‘\n";

        String expectedString = "Test list:\n" +
                "\n" +
                "\"one\"\n" +
                "'two'\n";


        assertEquals(expectedString, sanitizeCharacters(str));
    }
}

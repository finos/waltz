package org.finos.waltz.common;

import org.junit.jupiter.api.Test;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class StreamUtilities_linesTest {

    @Test
    public void simpleCase(){
        Stream<String> emptyLine = StreamUtilities
                .lines("");

        assertEquals("", emptyLine.collect(Collectors.joining("")));

    }


    @Test
    public void multiLines(){
        Stream<String> output = StreamUtilities
                .lines("a\nb\nc");

        assertEquals("a#b#c", output.collect(Collectors.joining("#")));

    }


    @Test
    public void onlyOneLine(){
        Stream<String> output = StreamUtilities
                .lines("ddd");

        assertEquals("<ddd>", output.collect(Collectors.joining("#", "<", ">")));

    }


    @Test
    public void emptyLines(){
        Stream<String> output = StreamUtilities
                .lines("hello\n\nworld");

        assertEquals("<hello##world>", output.collect(Collectors.joining("#", "<", ">")));
    }
}

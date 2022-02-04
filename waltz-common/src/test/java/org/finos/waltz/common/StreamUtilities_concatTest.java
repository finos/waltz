package org.finos.waltz.common;

import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class StreamUtilities_concatTest {
    @Test
    public void simpleConcat(){
        Collection<String> elements = ListUtilities.newArrayList("a", "b");
        Stream t = StreamUtilities.concat(elements);
        assertEquals(2, t.count());
    }

    @Test
    public void simpleConcatWithEmptyColl(){
        Collection<String> elements = ListUtilities.newArrayList();
        Stream t = StreamUtilities.concat(elements);
        assertEquals(0, t.count());
    }

    @Test
    public void simpleConcatWithNullColl() {
        Collection<String> elements = null;
        Stream t = StreamUtilities.concat(elements);
        assertThrows(NullPointerException.class,
                () -> t.count());
    }
}

package com.khartec.waltz.common;

import org.junit.Test;

import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static junit.framework.Assert.assertEquals;

public class StreamUtilities_concat {
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

    @Test(expected = NullPointerException.class)
    public void simpleConcatWithNullColl(){
        Collection<String> elements = null;
        Stream t = StreamUtilities.concat(elements);
        assertEquals(0, t.count());
    }
}

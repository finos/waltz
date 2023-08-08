package org.finos.waltz.common;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.finos.waltz.common.ListUtilities.getOrDefault;
import static org.finos.waltz.common.TestUtilities.assertLength;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ListUtilities_getOrDefaultTest {

    @Test
    public void getOrDefaultTests(){
        List<String> arr = ListUtilities.newArrayList("a", "b");
        assertEquals("a", getOrDefault(arr, 0, "z"));
        assertEquals("b", getOrDefault(arr, 1, "z"));
        assertEquals("z", getOrDefault(arr, 100, "z"));
        assertEquals("z", getOrDefault(arr, -1, "z"));

    }
}

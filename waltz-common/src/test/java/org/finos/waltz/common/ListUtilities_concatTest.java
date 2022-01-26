package org.finos.waltz.common;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.finos.waltz.common.TestUtilities.assertLength;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ListUtilities_concatTest {

    @Test
    public void concatSingleListElement(){
        List<String>  element = ListUtilities.newArrayList("a");
        List<String> result = ListUtilities.concat(element);
        TestUtilities.assertLength(result, 1);
        assertEquals("a", result.get(0));
    }

    @Test
    public void concatEmptyListElement(){
        List<String>  element = ListUtilities.newArrayList("");
        List<String> result = ListUtilities.concat(element);
        TestUtilities.assertLength(result, 1);
        assertEquals("", result.get(0));
    }
}

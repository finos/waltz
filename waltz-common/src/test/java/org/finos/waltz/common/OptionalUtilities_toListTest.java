package org.finos.waltz.common;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class OptionalUtilities_toListTest {
    @Test
    public void simpleToList(){
        Optional<String> ele1 = Optional.of("a");
        Optional<String> ele2 = Optional.of("b");
        List<String> result = OptionalUtilities.toList(ele1,ele2);
        assertEquals(2, result.size());
        assertEquals("a", result.get(0));
        assertEquals("b", result.get(1));
    }

    @Test
    public void simpleToListWithEmptyEle(){
        Optional ele = Optional.of("");
        List<String> result = OptionalUtilities.toList(ele);
        assertEquals(1, result.size());
        assertEquals("", result.get(0));
    }

    @Test
    public void simpleToListWithNullEle(){
        List<String> result = OptionalUtilities.toList(null);
        assertEquals(0, result.size());
    }

    @Test
    public void simpleToListWithOneNullEle() {
        Optional<String> ele1 = Optional.of("a");
        Optional<String> ele2 = null;
        assertThrows(NullPointerException.class,
                () -> OptionalUtilities.toList(ele1, ele2));
    }
}

package org.finos.waltz.common;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ListUtilities_isEmptyTest {

    @Test
    public void sendNullElement(){
        boolean result = ListUtilities.isEmpty(null);
        assertEquals(true,result);
    }

    @Test
    public void sendEmptyList(){
        List<String> element = ListUtilities.newArrayList();
        boolean result = ListUtilities.isEmpty(element);
        assertEquals(true,result);
    }

    @Test
    public void sendEmptyElement(){
        List<String> element = ListUtilities.newArrayList("");
        boolean result = ListUtilities.isEmpty(element);
        assertEquals(false,result);
    }

    @Test
    public void sendNonEmptyElement(){
        List<String> element = ListUtilities.newArrayList("a");
        boolean result = ListUtilities.isEmpty(element);
        assertEquals(false,result);
    }
}

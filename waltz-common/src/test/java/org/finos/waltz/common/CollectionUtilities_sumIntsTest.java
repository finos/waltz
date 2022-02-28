package org.finos.waltz.common;


import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CollectionUtilities_sumIntsTest {

    @Test
    public void sendEmptyList() {
        assertEquals(0, CollectionUtilities.sumInts(new ArrayList<>()).longValue());
    }

    @Test
    public void sendOne(){
        Collection<Integer> element = new ArrayList<>();
        element.add(1);
        assertEquals(1, CollectionUtilities.sumInts(element).longValue());
    }
}

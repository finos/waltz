package org.finos.waltz.common;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public class CollectionUtilities_maybeTest {
    @Test
    public void sendNullCollection(){
        Consumer display = a -> System.out.println("Consumed: "+a);
        CollectionUtilities.maybe(null, display);
    }

    @Test
    public void sendNullCollectionAndComaparator(){
        CollectionUtilities.maybe(null, null);
    }

    @Test
    public void sendEmptyCollection(){
        Consumer display = a -> System.out.println("Consumed: "+a);
        CollectionUtilities.maybe(Collections.EMPTY_LIST, display);
    }

    @Test
    public void sendElements(){
        List<Integer> elements = new ArrayList<Integer>();
        elements.add(2);
        elements.add(1);
        elements.add(3);
        Consumer display = a -> System.out.println("Consumed: "+a);
        CollectionUtilities.maybe(elements, display);
    }
}

package com.khartec.waltz.common;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import static com.khartec.waltz.common.CollectionUtilities.maybe;

public class CollectionUtilities_maybe {
    @Test
    public void sendNullCollection(){
        Consumer display = a -> System.out.println("Consumed: "+a);
        maybe(null, display);
    }

    @Test
    public void sendNullCollectionAndComaparator(){
        maybe(null, null);
    }

    @Test
    public void sendEmptyCollection(){
        Consumer display = a -> System.out.println("Consumed: "+a);
        maybe(Collections.EMPTY_LIST, display);
    }

    @Test
    public void sendElements(){
        List<Integer> elements = new ArrayList<Integer>();
        elements.add(2);
        elements.add(1);
        elements.add(3);
        Consumer display = a -> System.out.println("Consumed: "+a);
        maybe(elements, display);
    }
}

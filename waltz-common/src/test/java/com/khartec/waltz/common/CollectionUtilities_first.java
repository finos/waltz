package com.khartec.waltz.common;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

import static com.khartec.waltz.common.CollectionUtilities.first;
import static com.khartec.waltz.common.CollectionUtilities.maybeFirst;
import static com.khartec.waltz.common.ListUtilities.newArrayList;
import static org.junit.Assert.assertEquals;

public class CollectionUtilities_first {

    @Test(expected=IllegalArgumentException.class)
    public void sendNull(){
        assertEquals(first(null), Optional.empty());
    }

    @Test
    public void sendElements(){
        Collection element = new ArrayList();
        element.add("a");
        element.add("b");
        Object result = first(element);
        assertEquals("a", result);
    }
}

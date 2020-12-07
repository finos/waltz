package com.khartec.waltz.common;

import org.junit.Test;

import java.util.Collection;
import java.util.Collections;

import static com.khartec.waltz.common.CollectionUtilities.isEmpty;
import static org.junit.Assert.assertEquals;

public class CollectionUtilities_isEmpty {

    @Test
    public void sendNull(){
        assertEquals(true,isEmpty(null));

    }

    @Test
    public void sendEmpty(){
        assertEquals(true,isEmpty(Collections.EMPTY_LIST));
    }

    @Test
    public void sendElement(){
        Collection<String> element = Collections.singleton("a");
        assertEquals(false,isEmpty(element));
    }
}

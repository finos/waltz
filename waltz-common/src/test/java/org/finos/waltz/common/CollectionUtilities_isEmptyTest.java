package org.finos.waltz.common;

import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Collections;

import static org.finos.waltz.common.CollectionUtilities.isEmpty;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class CollectionUtilities_isEmptyTest {

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

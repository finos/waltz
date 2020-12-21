package com.khartec.waltz.common;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;

import static com.khartec.waltz.common.CollectionUtilities.sumInts;
import static org.junit.Assert.assertEquals;

public class CollectionUtilities_sumInts {

    @Test
    public void sendEmptyList(){
        Collection<Integer> element = new ArrayList();
        assertEquals(0, sumInts(element).longValue());
    }

    @Test
    public void sendOne(){
        Collection<Integer> element = new ArrayList();
        element.add(1);
        assertEquals(1, sumInts(element).longValue());
    }
}

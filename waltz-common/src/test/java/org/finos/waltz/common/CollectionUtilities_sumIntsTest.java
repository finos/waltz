package org.finos.waltz.common;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;

import static org.junit.Assert.assertEquals;

public class CollectionUtilities_sumIntsTest {

    @Test
    public void sendEmptyList(){
        Collection<Integer> element = new ArrayList();
        Assert.assertEquals(0, CollectionUtilities.sumInts(element).longValue());
    }

    @Test
    public void sendOne(){
        Collection<Integer> element = new ArrayList();
        element.add(1);
        Assert.assertEquals(1, CollectionUtilities.sumInts(element).longValue());
    }
}

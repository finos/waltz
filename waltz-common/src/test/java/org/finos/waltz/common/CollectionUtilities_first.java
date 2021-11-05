package org.finos.waltz.common;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

import static org.finos.waltz.common.CollectionUtilities.maybeFirst;
import static org.junit.Assert.assertEquals;

public class CollectionUtilities_first {

    @Test(expected=IllegalArgumentException.class)
    public void sendNull(){
        Assert.assertEquals(CollectionUtilities.first(null), Optional.empty());
    }

    @Test
    public void sendElements(){
        Collection element = new ArrayList();
        element.add("a");
        element.add("b");
        Object result = CollectionUtilities.first(element);
        assertEquals("a", result);
    }
}

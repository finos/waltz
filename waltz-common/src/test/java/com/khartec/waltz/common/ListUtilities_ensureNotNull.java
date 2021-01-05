package com.khartec.waltz.common;

import org.junit.Test;

import java.util.Collection;
import java.util.List;

import static com.khartec.waltz.common.TestUtilities.assertLength;
import static org.junit.Assert.assertEquals;

public class ListUtilities_ensureNotNull {

    @Test
    public void sendNullCollection(){
        Collection element = null;
        List result = ListUtilities.ensureNotNull(element);
        assertLength(result, 0);
    }

    @Test
    public void sendNonNullCollection(){
        Collection element = ListUtilities.newArrayList("a");
        List result = ListUtilities.ensureNotNull(element);
        assertLength(result, 1);
        assertEquals("a", result.get(0));
    }

    @Test
    public void sendEmptyStringCollection(){
        Collection element = ListUtilities.newArrayList("");
        List result = ListUtilities.ensureNotNull(element);
        assertLength(result, 1);
        assertEquals("", result.get(0));
    }
}

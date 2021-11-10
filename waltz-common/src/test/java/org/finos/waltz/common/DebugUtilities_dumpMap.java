package org.finos.waltz.common;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class DebugUtilities_dumpMap {
    @Test(expected = NullPointerException.class)
    public void dumpNullMap(){
        Map m = null;
        DebugUtilities.dump(m);
    }

    @Test
    public void dumpEmptyMap(){
        Map m = new HashMap();
        assertNotNull(DebugUtilities.dump(m));
    }

    @Test
    public void dumpMapElements(){
        Map m = new HashMap();
        m.put(1, "a");
        Map result = DebugUtilities.dump(m);
        assertEquals(m.size(), result.size());
        assertEquals("a", result.get(1));
    }

}

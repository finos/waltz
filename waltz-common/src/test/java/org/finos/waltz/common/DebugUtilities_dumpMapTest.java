package org.finos.waltz.common;

import org.junit.jupiter.api.Test;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class DebugUtilities_dumpMapTest {
    @Test
    public void dumpNullMap() {
        Map m = null;  // Weird test, we test that we get a NullPointerException??
        assertThrows(NullPointerException.class,
                () -> DebugUtilities.dump(m));
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

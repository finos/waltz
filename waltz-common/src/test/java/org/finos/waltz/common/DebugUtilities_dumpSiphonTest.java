package org.finos.waltz.common;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class DebugUtilities_dumpSiphonTest {
    @Test
    public void dumpNullSiphon() {
        assertThrows(NullPointerException.class,
                () -> DebugUtilities.dump("aaa", null));
    }

    @Test
    public void dumpEmptyPreamble(){
        StreamUtilities.Siphon s = new StreamUtilities.Siphon(i->i!=null);
        DebugUtilities.dump("",s);
    }

    @Test
    public void dumpNullPreamble(){
        StreamUtilities.Siphon s = new StreamUtilities.Siphon(i->i!=null);
        DebugUtilities.dump(null,s);
    }

    @Test
    public void dumpMapElements(){
        StreamUtilities.Siphon s = new StreamUtilities.Siphon(i->i!=null);
        DebugUtilities.dump("aaa",s);
    }
}

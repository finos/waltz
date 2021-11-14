package org.finos.waltz.common;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;

public class DebugUtilities_dumpSiphonTest {
    @Test(expected = NullPointerException.class)
    public void dumpNullSiphon(){
        StreamUtilities.Siphon s = null;
        DebugUtilities.dump("aaa",s);
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

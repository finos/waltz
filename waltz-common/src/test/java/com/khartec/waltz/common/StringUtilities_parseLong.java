package com.khartec.waltz.common;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class StringUtilities_parseLong {

    @Test
    public void simpleParseLong(){
        Long val = Long.valueOf(1);
        Long def = Long.valueOf(0);
        assertEquals(val, StringUtilities.parseLong("1",def));
    }

    @Test
    public void simpleParseLongWithEmptyStr(){
        Long def = Long.valueOf(0);
        assertEquals(def, StringUtilities.parseLong("",def));
    }

    @Test
    public void simpleParseLongWithNullStr(){
        Long def = Long.valueOf(0);
        assertEquals(def, StringUtilities.parseLong(null,def));
    }

    @Test
    public void simpleParseLongWithNullDef(){
        Long def = null;
        assertEquals(def, StringUtilities.parseLong("",def));
    }

    @Test
    public void simpleParseLongWithBothNull(){
        Long def = null;
        assertEquals(def, StringUtilities.parseLong(null,def));
    }
}

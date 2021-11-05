package org.finos.waltz.common;

import org.junit.Before;
import org.junit.Test;

import static org.finos.waltz.common.EnumUtilities.parseEnumWithAliases;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class EnumUtilities_parseEnumWithAliases {

    private enum MyEnum {
        A,
        B
    }

    private Aliases<MyEnum> al = new Aliases();


    @Before
    public void before() {
        al.register(MyEnum.A, "a", "synonym-for-a");
        al.register(MyEnum.B, "b");
    }


    @Test(expected = IllegalArgumentException.class)
    public void parseWithNullValue(){
        assertNull(parseEnumWithAliases(null, MyEnum.class, null, al));
    }


    @Test(expected = IllegalArgumentException.class)
    public void parseWithEmptyValue(){
        assertNull(parseEnumWithAliases("", MyEnum.class, null, al));
    }


    @Test(expected = IllegalArgumentException.class)
    public void parseWithNullEnumClass(){
        assertEquals(
                MyEnum.A,
                parseEnumWithAliases("a", null, null, al));
    }


    @Test
    public void parseWithNullAlias(){
        assertEquals(
                MyEnum.A,
                parseEnumWithAliases("a", MyEnum.class, null, null));
    }


    @Test(expected = IllegalArgumentException.class)
    public void parseWithAllNullParams(){
        parseEnumWithAliases(null, null, null, null);
    }


    @Test(expected = IllegalArgumentException.class)
    public void parseWithEmptyValAndNullOtherParams(){
        parseEnumWithAliases("", null, null, null);
    }


    @Test
    public void parseWithNonNullParams(){
        assertEquals(
                MyEnum.A,
                parseEnumWithAliases("synonym-for-a", MyEnum.class, null, al));
    }


    @Test
    public void parseWithNonFallback(){
        assertEquals(
                MyEnum.B,
                parseEnumWithAliases("wibble", MyEnum.class, x -> MyEnum.B, al));
    }
}

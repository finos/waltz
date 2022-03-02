package org.finos.waltz.common;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.finos.waltz.common.EnumUtilities.parseEnumWithAliases;
import static org.junit.jupiter.api.Assertions.*;

public class EnumUtilities_parseEnumWithAliasesTest {

    private enum MyEnum {
        A,
        B
    }

    private Aliases<MyEnum> al = new Aliases();


    @BeforeEach
    public void before() {
        al.register(MyEnum.A, "a", "synonym-for-a");
        al.register(MyEnum.B, "b");
    }


    @Test
    public void parseWithNullValue() {
        assertThrows(IllegalArgumentException.class,
                () -> parseEnumWithAliases(null, MyEnum.class, null, al));
    }


    @Test
    public void parseWithEmptyValue() {
        assertThrows(IllegalArgumentException.class,
                () -> parseEnumWithAliases("", MyEnum.class, null, al));
    }


    @Test
    public void parseWithNullEnumClass() {
        assertThrows(IllegalArgumentException.class,
                () -> parseEnumWithAliases("a", null, null, al));
    }


    @Test
    public void parseWithNullAlias(){
        assertEquals(
                MyEnum.A,
                parseEnumWithAliases("a", MyEnum.class, null, null));
    }

    public void parseWithAllNullParams() {
        assertThrows(IllegalArgumentException.class,
                () -> parseEnumWithAliases(null, null, null, null));

    }


    @Test
    public void parseWithEmptyValAndNullOtherParams() {
        assertThrows(IllegalArgumentException.class,
                () -> parseEnumWithAliases("", null, null, null));
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

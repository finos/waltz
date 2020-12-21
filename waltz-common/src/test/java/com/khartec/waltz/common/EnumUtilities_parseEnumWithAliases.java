package com.khartec.waltz.common;

import org.junit.Test;

public class EnumUtilities_parseEnumWithAliases {
    private enum MyEnum {
        A,
        B
    }

    @Test(expected = IllegalArgumentException.class)
    public void parseWithNullValue(){
        Aliases al = new Aliases();
        al.register(MyEnum.A, "a");
        al.register(MyEnum.B, "b");
        EnumUtilities.parseEnumWithAliases(null, MyEnum.class, null, al);
    }

    @Test(expected = IllegalArgumentException.class)
    public void parseWithEmptyValue(){
        Aliases al = new Aliases();
        al.register(MyEnum.A, "a");
        al.register(MyEnum.B, "b");
        EnumUtilities.parseEnumWithAliases("", MyEnum.class, null, al);
    }

    @Test(expected = IllegalArgumentException.class)
    public void parseWithNullEnumClass(){
        Aliases al = new Aliases();
        al.register(MyEnum.A, "a");
        al.register(MyEnum.B, "b");
        EnumUtilities.parseEnumWithAliases("a", null, null, al);
    }

    @Test
    public void parseWithNullAlias(){
        EnumUtilities.parseEnumWithAliases("a", MyEnum.class, null, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void parseWithAllNullParams(){
        EnumUtilities.parseEnumWithAliases(null, null, null, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void parseWithEmptyValAndNullOtherParams(){
        EnumUtilities.parseEnumWithAliases("", null, null, null);
    }

    @Test
    public void parseWithNonNullParams(){
        Aliases al = new Aliases();
        al.register(MyEnum.A, "a");
        al.register(MyEnum.B, "b");
        EnumUtilities.parseEnumWithAliases("a", MyEnum.class, null, al);
    }
}

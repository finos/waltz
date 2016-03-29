package com.khartec.waltz.common;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import static com.khartec.waltz.common.EnumUtilities.readEnum;
import static org.junit.Assert.assertEquals;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class EnumUtilities_readEnum {

    private enum MyEnum {
        A,
        B
    }


    @Test
    public void canReadEnum() {
        assertEquals(MyEnum.A, readEnum("A", MyEnum.class, null));
        assertEquals(MyEnum.B, readEnum("B", MyEnum.class, null));
    }


    @Test
    public void defaultValueUsedIfNoMatch() {
        assertEquals(MyEnum.A, readEnum("Z", MyEnum.class, MyEnum.A));
        assertEquals(null, readEnum("Z", MyEnum.class, null));
    }


    @Test(expected = IllegalArgumentException.class)
    public void badIfNoEnumClass() {
        readEnum("A", null, MyEnum.A);
    }

}

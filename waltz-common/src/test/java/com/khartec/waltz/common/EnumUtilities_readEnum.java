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
        assertEquals(MyEnum.A, readEnum("A", MyEnum.class, (s) -> null));
        assertEquals(MyEnum.B, readEnum("B", MyEnum.class, (s) -> null));
    }


    @Test
    public void defaultValueUsedIfNoMatch() {
        assertEquals(MyEnum.A, readEnum("Z", MyEnum.class, (s) -> MyEnum.A));
        assertEquals(null, readEnum("Z", MyEnum.class, (s) -> null));
    }


    @Test(expected = IllegalArgumentException.class)
    public void badIfNoEnumClass() {
        readEnum("A", null, (s) -> MyEnum.A);
    }

}

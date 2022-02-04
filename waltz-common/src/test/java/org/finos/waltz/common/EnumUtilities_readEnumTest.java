/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017, 2018, 2019 Waltz open source project
 * See README.md for more information
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific
 *
 */

package org.finos.waltz.common;


import org.junit.jupiter.api.Test;

import static org.finos.waltz.common.EnumUtilities.parseEnumWithAliases;
import static org.finos.waltz.common.EnumUtilities.readEnum;
import static org.junit.jupiter.api.Assertions.*;


public class EnumUtilities_readEnumTest {

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
        assertNull(readEnum("Z", MyEnum.class, (s) -> null));
    }

}

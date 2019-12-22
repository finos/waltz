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

package com.khartec.waltz.common;

import org.junit.Test;

import java.time.LocalDate;
import java.util.Date;

import static junit.framework.TestCase.assertNull;

public class DateTimeUtilitiesTest {

    @Test
    public void dateTimeConversionsReturnNullIfGivenNull() {
        assertNull(DateTimeUtilities.toSqlDate((Date) null));
        assertNull(DateTimeUtilities.toSqlDate((LocalDate) null));
        assertNull(DateTimeUtilities.toLocalDateTime(null));
        assertNull(DateTimeUtilities.toLocalDate(null));
    }

}

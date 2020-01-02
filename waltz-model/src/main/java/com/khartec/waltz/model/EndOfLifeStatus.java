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

package com.khartec.waltz.model;


import java.time.LocalDate;
import java.util.Date;

import static com.khartec.waltz.common.DateTimeUtilities.toLocalDate;

public enum EndOfLifeStatus {

    END_OF_LIFE,
    NOT_END_OF_LIFE;


    public static EndOfLifeStatus calculateEndOfLifeStatus(Date eolDate) {
        if (eolDate != null
                && toLocalDate(eolDate).isBefore(LocalDate.now())) {
            return END_OF_LIFE;
        }
        return NOT_END_OF_LIFE;
    }
}

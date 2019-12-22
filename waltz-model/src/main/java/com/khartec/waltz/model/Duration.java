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

public enum Duration {

    DAY(1),
    WEEK(7),
    MONTH(31),
    QUARTER(MONTH.numDays * 3),
    HALF_YEAR(MONTH.numDays * 6),
    YEAR(MONTH.numDays * 12),
    ALL(Integer.MAX_VALUE);


    private final int numDays;


    Duration(int numDays) {
        this.numDays = numDays;
    }


    public int numDays() {
        return numDays;
    }
}

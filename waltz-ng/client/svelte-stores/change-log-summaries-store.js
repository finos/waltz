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

import {remote} from "./remote";

export function mkStore()  {
    const BASE_URL = "api/change-log-summaries";

    const findYearOnYearChanges = (parentKind, childKind, force) => remote
        .fetchViewDatum("GET",
            `${BASE_URL}/year_on_year?parentKind=${parentKind}&childKind=${childKind}`,
            null,
            {force});

    const findChangeLogYears = (force) => remote
        .fetchViewList("GET",
            `${BASE_URL}/get_years`,
            null,
            {force});

    const findMonthOnMonthChanges = (parentKind, childKind, year, force) => remote
        .fetchViewDatum("GET",
            `${BASE_URL}/month_on_month?parentKind=${parentKind}&childKind=${childKind}&year=${year}`,
            null,
            {force});


    return {
        findYearOnYearChanges,
        findChangeLogYears,
        findMonthOnMonthChanges
    };
}

export const changeLogSummariesStore = mkStore();
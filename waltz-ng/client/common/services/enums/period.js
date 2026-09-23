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

// Calendar bucket used by the analytics dashboard. Keys mirror the backend
// org.finos.waltz.model.Duration enum so the value can be sent to the api as-is.
export const period = {
    DAY: {
        key: 'DAY',
        name: 'Day',
        icon: null,
        description: null,
        position: 10
    },
    WEEK: {
        key: 'WEEK',
        name: 'Week',
        icon: null,
        description: null,
        position: 20
    },
    MONTH: {
        key: 'MONTH',
        name: 'Month',
        icon: null,
        description: null,
        position: 30
    },
    YEAR: {
        key: 'YEAR',
        name: 'Year',
        icon: null,
        description: null,
        position: 40
    }
};

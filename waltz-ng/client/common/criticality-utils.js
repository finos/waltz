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
import _ from "lodash";


const criticalities = [
    "UNKNOWN",
    "NONE",
    "LOW",
    "MEDIUM",
    "HIGH",
    "VERY_HIGH",
];


const cmp = (a, b) => a > b ? +1 : a < b ? -1 : 0;


export function compareCriticalities(a, b) {
    const aIdx = _.indexOf(criticalities, a);
    if (aIdx === -1) {
        throw "Not a recognized criticality" + a;
    }
    const bIdx = _.indexOf(criticalities, b);
    if (bIdx === -1) {
        throw "Not a recognized criticality" + a;
    }

    return cmp(aIdx, bIdx);
}
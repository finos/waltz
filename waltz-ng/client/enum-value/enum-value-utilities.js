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

/**
 * Given a list of enums will filter by `type` and then index the resultant list
 * by the key.  Each item returned looks like:
 * ```
 * { key, name, data: {...} }
 * ```
 * @param enums  list of enums to filter and index
 * @param type  type of enums to filter (keep)
 * @returns {*}
 */
export default function indexByKeyForType(enums = [], type) {
    return _
        .chain(enums)
        .filter(d => d.type === type)
        .map(d => ({ key: d.key, name: d.name, data: d }))
        .keyBy("key")
        .value();
}
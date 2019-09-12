/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017, 2018, 2019 Waltz open source project
 * See README.md for more information
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
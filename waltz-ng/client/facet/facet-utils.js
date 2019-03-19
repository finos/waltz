/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017  Waltz open source project
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

import { viewStateToKind } from "../common/link-utils";


export function areFiltersVisible(viewStateName) {
    try {
        const kind = viewStateToKind(viewStateName);
        switch (kind) {
            case "APP_GROUP":
            case "DATA_TYPE":
            case "MEASURABLE":
            case "ORG_UNIT":
            case "ENTITY_STATISTIC":
            case "PERSON":
                return true;
            default:
                return false;
        }
    } catch (e) {
        // in case of an unknown viewstate
        return false;
    }
}
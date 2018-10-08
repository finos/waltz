/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017 Waltz open source project
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


import {checkIsEntityRef} from "./checks";

export function getDefaultScopeForEntityKind(kind) {
    switch (kind) {
        case "ACTOR":
        case "APPLICATION":
        case "APP_GROUP":
        case "CHANGE_INITIATIVE":
        case "FLOW_DIAGRAM":
        case "LOGICAL_DATA_ELEMENT":
        case "LOGICAL_FLOW":
        case "PHYSICAL_FLOW":
        case "PHYSICAL_SPECIFICATION":
        case "SCENARIO":
            return "EXACT";
        default:
            return "CHILDREN";
    }
}


export function mkSelectionOptions(entityReference, scope, entityLifecycleStatuses = ["ACTIVE"]) {
    checkIsEntityRef(entityReference);

    return {
        entityReference,
        scope: scope || getDefaultScopeForEntityKind(entityReference.kind),
        entityLifecycleStatuses
    };
}
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

import {CORE_API} from "./services/core-api-utils";

export const allowedRelationshipsByKind = {
    "CHANGE_INITIATIVE": [{
        value: "APPLICATION_NEW",
        name: "Application - new"
    }, {
        value: "APPLICATION_FUNCTIONAL_CHANGE",
        name: "Application - functional change"
    }, {
        value: "APPLICATION_DECOMMISSIONED",
        name: "Application - decommissioned"
    }, {
        value: "APPLICATION_NFR_CHANGE",
        name: "Application - NFR change"
    }, {
        value: "DATA_PUBLISHER",
        name: "Data publisher"
    }, {
        value: "DATA_CONSUMER",
        name: "Data consumer"
    }]
};


export const fetchRelationshipFunctionsByKind = {
    "CHANGE_INITIATIVE": CORE_API.ChangeInitiativeStore.findRelatedForId
};


export const changeRelationshipFunctionsByKind = {
    "CHANGE_INITIATIVE": CORE_API.ChangeInitiativeStore.changeRelationship
};


export function mkRel(a, relationship, b) {
    if (a.kind === "ROADMAP") {
        const tmp = a;
        a = b;
        b = tmp;
    }
    return {a, b, relationship};
}

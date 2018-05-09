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

import {rollupKind} from "../common/services/enums/rollup-kind";
import {notEmpty} from "../common";


export function updateUrlWithoutReload($state, navItem) {
    $state.go('.', {id: navItem.id}, {notify: false});
}


const defaultDefinitions = {
    children: [],
    parent: null
};


export function hasRelatedDefinitions(definitions = defaultDefinitions) {
    return notEmpty(definitions.children);
}


export function navigateToStatistic($state, statisticId, parentEntityReference) {
    const params = {
        id: parentEntityReference.id,
        kind: parentEntityReference.kind,
        statId: statisticId
    };

    const stateName = parentEntityReference.kind === 'PERSON'
        ? "main.entity-statistic.view-person"
        : "main.entity-statistic.view";

    $state.go(stateName, params);
}


export function mkSummaryTableHeadings(definition) {
    return [
        "Outcome",
        mkValueHeading(definition),
        "%"
    ];
}


function mkValueHeading(definition) {
    if (!definition) {
        return "";
    }
    return rollupKind[definition.rollupKind] ? rollupKind[definition.rollupKind].name : '-';
}




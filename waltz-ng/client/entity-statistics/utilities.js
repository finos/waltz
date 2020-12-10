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

import {rollupKind} from "../common/services/enums/rollup-kind";
import {notEmpty} from "../common";


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




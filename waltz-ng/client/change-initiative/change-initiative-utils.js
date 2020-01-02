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

import * as _ from "lodash";

export function groupRelationships(ciId, rels = []) {
    return _.chain(rels)
        .flatMap(rel => ([
            {entity: rel.a, relationship: rel.relationship},
            {entity: rel.b, relationship: rel.relationship}
        ]))
        .reject(rel => rel.entity.kind === 'CHANGE_INITIATIVE' && rel.entity.id === ciId)
        .groupBy('entity.kind', 'entity.id')
        .value();
}


export function enrichRelationships(rels = [], entities = []) {
    const entitiesById = _.keyBy(entities, 'id');

    return _.chain(rels)
        .map(rel => Object.assign({}, {
            relationship: rel.relationship,
            entity: entitiesById[rel.entity.id]
        }))
        .filter(rel => rel.entity)
        .value();
}


export const fakeProgramme = {
    id: -2,
    parentId: -1,
    isFake: true,
    externalId: "-",
    name: "Programme Placeholder",
    description: "Placeholder programme as there is no actual linked programme",
    kind: "CHANGE_INITIATIVE",
    changeInitiativeKind: "INITIATIVE"
};

export const fakeInitiative = {
    id: -1,
    parentId: null,
    externalId: "-",
    isFake: true,
    name: "Initiative Placeholder",
    description: "Placeholder programme as there is no actual linked initiative",
    kind: "CHANGE_INITIATIVE",
    changeInitiativeKind: "INITIATIVE"
};

export const fakeParentsByChildKind = {
    "PROJECT": fakeProgramme,
    "PROGRAMME": fakeInitiative
};


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


export function aggregatePeopleInvolvements(involvements = [], people = []) {
    const involvementsByPerson = _
        .chain(involvements)
        .groupBy('employeeId')
        .mapValues(xs => _  // dedupe involvement kinds for person
            .chain(xs)
            .map(x => ({
                kindId: x.kindId,
                provenance: x.provenance,
                isReadOnly: x.isReadOnly
            }))
            .uniqBy(d => d.kindId)
            .value()
        )
        .value();

    return _
        .chain(people)
        .map(person => ({person, involvements: involvementsByPerson[person.employeeId]}))
        .uniqBy(i => i.person.id)
        .value();
}


export function aggregatePeopleByKeyInvolvementKind(involvements, people, keyInvolvementKinds = []) {
    const peopleById = _.keyBy(people, "employeeId");

    const aggregatePeopleByInvolvementKind = _
        .chain(involvements)
        .map(inv => ({
            person: peopleById[inv.employeeId],
            involvement: ({kindId: inv.kindId})
        }))
        .groupBy(inv => inv.involvement.kindId)
        .value();

    return _.chain(keyInvolvementKinds)
        .map(kind => ({
            roleName: kind.name,
            persons: aggregatePeopleByInvolvementKind[kind.id]
        }))
        .sortBy("roleName")
        .value();
}

export function mkChangeCommand(operation, personEntityRef, involvementKindId) {
    return {
        operation,
        personEntityRef,
        involvementKindId
    };
}


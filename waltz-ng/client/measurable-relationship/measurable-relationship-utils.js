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
import {checkIsEntityRef} from "../common/checks";
import {sameRef} from "../common/entity-utils";


export function determineCounterpart(reference, relationship, options = { skipChecks: false }) {
    if (!options.skipChecks) {
        checkIsEntityRef(reference);
    }
    return sameRef(reference, relationship.a, options)
        ? relationship.b
        : relationship.a;
}


export function sanitizeRelationships(relationships, measurables, categories) {
    const categoriesById = _.keyBy(categories, c => c.id);
    const measurablesById = _.keyBy(measurables, m => m.id);

    const isValid = (mId) => measurablesById[mId] && categoriesById[measurablesById[mId].categoryId];

    const isValidRel = (rel) => {
        const validCombos = [
            { a: "MEASURABLE", b: "APP_GROUP" },
            { a: "MEASURABLE", b: "CHANGE_INITIATIVE" },
            { a: "MEASURABLE", b: "MEASURABLE" },
            { a: "CHANGE_INITIATIVE", b: "APP_GROUP" },
            { a: "CHANGE_INITIATIVE", b: "CHANGE_INITIATIVE" },
            { a: "CHANGE_INITIATIVE", b: "MEASURABLE" },
            { a: "APP_GROUP", b: "APP_GROUP" },
            { a: "APP_GROUP", b: "CHANGE_INITIATIVE" },
            { a: "APP_GROUP", b: "MEASURABLE" }
        ];

        const validCombo = _.some(validCombos, c => c.a === rel.a.kind && c.b === rel.b.kind);

        const aValid = rel.a.kind === "MEASURABLE"
            ? isValid(rel.a.id)
            : true;
        const bValid = rel.b.kind === "MEASURABLE"
            ? isValid(rel.b.id)
            : true;

        return validCombo && aValid && bValid;
    };

    return _.filter(relationships, isValidRel);
}

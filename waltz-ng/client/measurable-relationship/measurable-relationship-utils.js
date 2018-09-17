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

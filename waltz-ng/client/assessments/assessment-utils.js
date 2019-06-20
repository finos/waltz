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
import {indexRatingSchemes} from "../ratings/rating-utils";
import {nest} from "d3-collection";
import {grey} from "../common/colors";

/**
 * Creates an enriched assessment definition which adds fields for
 * the possible dropdown values and (if set) the current rating along
 * with the rating item (describing the dropdown option)
 *
 * @param definitions
 * @param schemes
 * @param assessments
 */
export function mkEnrichedAssessmentDefinitions(definitions = [],
                                                schemes = [],
                                                assessments = []) {
    const schemesByIdByRatingId = indexRatingSchemes(schemes);
    const assessmentsByDefinitionId = _.keyBy(assessments, "assessmentDefinitionId");

    return _
        .chain(definitions)
        .map(definition => {
            const scheme = _.get(schemesByIdByRatingId, `[${definition.ratingSchemeId}]`);
            const assessment = _.get(assessmentsByDefinitionId, `[${definition.id}]`, null);
            const ratingSchemeItem = assessment != null
                ? _.get(scheme, `ratingsById[${assessment.ratingId}]`)
                : null;

            const dropdownEntries = _.map(
                scheme.ratings,
                r => Object.assign(
                    {},
                    r,
                    { code: r.id }));

            return {
                definition,
                rating: assessment,
                ratingItem: ratingSchemeItem,
                dropdownEntries };
        })
        .orderBy("name")
        .value();
}


export function mkAssessmentSummaries(definitions = [], schemes = [], ratings = [], total = 0) {
    const indexedRatingSchemes = indexRatingSchemes(schemes);
    const definitionsById = _.keyBy(definitions, d => d.id);

    console.log({ total })
    const nestedRatings = nest()
        .key(d => d.assessmentDefinitionId)
        .key(d => d.ratingId)
        .rollup(xs => xs.length)
        .entries(ratings);

    return _
        .chain(nestedRatings)
        .map(d => {
            const definition = definitionsById[Number(d.key)];
            const assignedTotal = _.sumBy(d.values, v => v.value);
            const values = _
                .chain(d.values)
                .map(v => {
                    const propPath = [definition.ratingSchemeId, "ratingsById", v.key];
                    const rating = _.get(indexedRatingSchemes, propPath);
                    return Object.assign({}, v, { rating, count: v.value });
                })
                .concat([{
                    key: "z",
                    rating: {
                        id: -1,
                        name: "Not Provided",
                        color: grey
                    },
                    count: _.max([0, total - assignedTotal])
                }])
                .filter(d => d.count > 0)
                .value();

            const extension = { definition, values };
            return Object.assign({}, d , extension);
        })
        .orderBy(d => d.definition.name)
        .value();
}

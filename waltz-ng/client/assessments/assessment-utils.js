/*
 * Waltz - Enterprise Architecture
 *  Copyright (C) 2016, 2017 Waltz open source project
 *  See README.md for more information
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
import _ from "lodash";
import {indexRatingSchemes} from "../ratings/rating-utils";

/**
 * Creates an enriched assessment definition which adds fields for
 * the possible dropdown values and (if set) the current rating along
 * with the rating item (describing the dropdown option)
 *
 * @param assessmentDefinitions
 * @param ratingSchemes
 * @param assessmentRatings
 */
export function mkEnrichedAssessmentDefinitions(assessmentDefinitions = [],
                                                ratingSchemes = [],
                                                assessmentRatings = []) {
    const ratingSchemesByIdByItemId = indexRatingSchemes(ratingSchemes);
    const ratingsByDefinitionId = _.keyBy(assessmentRatings, 'assessmentDefinitionId');

    return _
        .chain(assessmentDefinitions)
        .map(d => {
            const ratingScheme = _.get(ratingSchemesByIdByItemId, `[${d.ratingSchemeId}]`);
            const rating = _.get(ratingsByDefinitionId, `[${d.id}]`, null);
            const ratingItem = rating != null
                ? _.get(ratingScheme, `ratingsById[${rating.ratingId}]`)
                : null;

            const dropdownEntries = _.map(
                ratingScheme.ratings,
                r => Object.assign(
                    {},
                    { name: r.name, code: r.id, position: r.position }));

            return Object.assign({}, d, { rating, ratingItem, dropdownEntries });
        })
        .orderBy('name')
        .value();
}
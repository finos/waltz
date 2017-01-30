/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016  Khartec Ltd.
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
import _ from 'lodash';


import {
    checkAll,
    checkIsMeasurableRating,
    checkIsMeasurable,
    checkIsPerspectiveDefinition,
    checkNotEmpty,

} from '../common/checks';


function mkPerspective(perspectiveDefinition,
                       measurables = [],
                       ratings = []) {
    checkNotEmpty(perspectiveDefinition, "Must supply a perspective definition");
    checkIsPerspectiveDefinition(perspectiveDefinition);
    checkAll(measurables, checkIsMeasurable);
    checkAll(ratings, checkIsMeasurableRating);

    const measurablesById = _.keyBy(measurables, 'id');

    const mkRatingFilterFn = categoryId => ratings =>
        _.filter(ratings, r => {
            const measurable = measurablesById[r.measurableId];
            if (measurable) {
                return measurable.categoryId === categoryId;
            }
        });

    const mkAxis = (categoryId) => {
        const ratingFilter = mkRatingFilterFn(categoryId);
        const axisRatings = ratingFilter(ratings);
        return _
            .chain(axisRatings)
            .map(r => ({ rating: r, measurable: measurablesById[r.measurableId] }))
            .sortBy('measurable.name')
            .value();
    };

    return {
        definition: perspectiveDefinition,
        axes: {
            a: mkAxis(perspectiveDefinition.categoryA),
            b: mkAxis(perspectiveDefinition.categoryB)
        }
    };
}


export { mkPerspective };
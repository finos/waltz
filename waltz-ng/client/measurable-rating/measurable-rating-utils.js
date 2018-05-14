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


export function mkTabs(categories = [],
                       ratingSchemesById = {},
                       measurables = [],
                       ratings = []) {

    const measurablesByCategory = _.groupBy(measurables, 'categoryId');

    const tabs = _.map(categories, category => {
        const measurablesForCategory = measurablesByCategory[category.id] || [];
        const measurableIds = _.map(measurablesForCategory, 'id');
        const ratingsForCategory = _.filter(
            ratings,
            r => _.includes(measurableIds, r.measurableId));
        const ratingScheme = ratingSchemesById[category.ratingSchemeId];
        return {
            category,
            ratingScheme,
            measurables: measurablesForCategory,
            ratings: ratingsForCategory
        };
    });

    return _.sortBy(
        tabs,
        g => g.category.name);
}


export function determineStartingTab(tabs = []) {
    // first with ratings, or simply first if no ratings
    return _.find(tabs, t => t.ratings.length > 0 ) || tabs[0];
}



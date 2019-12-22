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


export function mkTabs(categories = [],
                       ratingSchemesById = {},
                       measurables = [],
                       ratings = [],
                       allocationSchemes = [],
                       includeEmpty = true) {

    const measurablesByCategory = _.groupBy(measurables, d => d.categoryId);
    const allocationSchemesByCategory = _.groupBy(allocationSchemes, d => d.measurableCategoryId);

    return _.chain(categories)
        .map(category => {
            const measurablesForCategory = measurablesByCategory[category.id] || [];
            const measurableIds = _.map(measurablesForCategory, "id");
            const ratingsForCategory = _.filter(
                ratings,
                r => _.includes(measurableIds, r.measurableId));
            const ratingScheme = ratingSchemesById[category.ratingSchemeId];
            return {
                category,
                ratingScheme,
                measurables: measurablesForCategory,
                ratings: ratingsForCategory,
                allocationSchemes: allocationSchemesByCategory[category.id] || []
            };
        })
        .filter(t => t.ratings.length > 0 || includeEmpty)
        .sortBy(tab => tab.category.name)
        .value();
}


export function determineStartingTab(tabs = []) {
    // first with ratings, or simply first if no ratings
    return _.find(tabs, t => t.ratings.length > 0 ) || tabs[0];
}



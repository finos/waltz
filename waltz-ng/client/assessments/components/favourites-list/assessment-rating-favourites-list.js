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

import {initialiseData} from "../../../common";
import _ from "lodash";
import template from "./assessment-rating-favourites-list.html";
import {mkAssessmentDefinitionsIdsBaseKey} from "../../../user";
import {CORE_API} from "../../../common/services/core-api-utils";
import {getIdsFromString} from "../../assessment-utils";


const bindings = {
    assessments: "<",
    parentEntityRef: "<"
};


const initialState = {
    assessmentsWithRatings: [],
    assessmentsWithoutRatings: [],
};


function isEmpty(favouritesString) {
    return _.isNull(favouritesString) || _.isEmpty(favouritesString);
}

function getFavouriteAssessmentDefnIds(baseKey, preferences, defaultList = []) {

    const favouriteIncludedKey = `${baseKey}.included`;
    const favouriteExcludedKey = `${baseKey}.excluded`;

    const includedFavouritesString = _.find(preferences, d => d.key === favouriteIncludedKey, null);
    const excludedFavouritesString = _.find(preferences, d => d.key === favouriteExcludedKey, null);

    if (isEmpty(includedFavouritesString) && isEmpty(excludedFavouritesString)) {
        return defaultList;
    } else {
        const incIds = getIdsFromString(includedFavouritesString);
        const excIds = getIdsFromString(excludedFavouritesString);

        return _.reject(
            _.concat(defaultList, incIds),
            d => _.includes(excIds, d));
    }
}


function controller(serviceBroker) {
    const vm = initialiseData(this, initialState);

    function isFavourite(id) {
        return _.includes(vm.favouriteAssessmentDefnIds, id);
    }

    const filterAssessments = () => {
        if (vm.assessments) {

            const filtered = _
                .chain(vm.assessments)
                .filter(a => isFavourite(a.definition.id))
                .value();

            const valuePartitioned = _.partition(
                filtered,
                assessment => _.isNil(assessment.rating));

            vm.assessmentsWithoutRatings = _.sortBy(valuePartitioned[0], d => d.definition.name);
            vm.assessmentsWithRatings = _.sortBy(valuePartitioned[1], d => d.definition.name);
        }
    };

    vm.$onChanges = () => {

        vm.defaultPrimaryList = _
            .chain(vm.assessments)
            .filter(a => a.definition.visibility === "PRIMARY")
            .map(r => r.definition.id)
            .value();

        serviceBroker
            .loadAppData(CORE_API.UserPreferenceStore.findAllForUser, [])
            .then(r => vm.favouriteAssessmentDefnIds = getFavouriteAssessmentDefnIds(
                mkAssessmentDefinitionsIdsBaseKey(vm.parentEntityRef),
                r.data,
                vm.defaultPrimaryList))
            .then(() => filterAssessments());
    };
}


controller.$inject = [
    "ServiceBroker"
];


const component = {
    template,
    bindings,
    controller
};


export default {
    component,
    id: "waltzAssessmentRatingFavouritesList"
};

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
import template from "./assessment-rating-list.html";
import {mkAssessmentDefinitionsIdsBaseKey} from "../../../user";
import {CORE_API} from "../../../common/services/core-api-utils";
import {displayError} from "../../../common/error-utils";
import toasts from "../../../svelte-stores/toast-store";
import {isFavourite} from "../../assessment-utils";

const bindings = {
    assessments: "<",
    parentEntityRef: "<",
    onSelect: "<",
};


const initialState = {
    assessmentsProvided: [],
    assessmentsNotProvided: [],
    expandNotProvided: false,
    favouritesKey: null,
};


function getFavouriteAssessmentDefnIds(key, preferences, defaultList = []) {
    const favouritesString = _.find(preferences, d => d.key === key, null);
    return _.isNull(favouritesString) || _.isEmpty(favouritesString)
        ? defaultList
        : _
            .chain(favouritesString.value)
            .split(",")
            .map(idString => _.toNumber(idString))
            .value();
}

function controller(serviceBroker) {
    const vm = initialiseData(this, initialState);


    const partitionAssessments = () => {
        if (vm.assessments) {
            const [notProvided, provided] = _
                .chain(vm.assessments)
                .sortBy(d => d.definition.name)
                .map(a => Object.assign({}, a, {isFavourite: isFavourite(vm.favouriteAssessmentDefnIds, a.definition.id)}))
                .partition(assessment => _.isNil(assessment.rating))
                .value();

            vm.assessmentsNotProvided = notProvided;
            vm.assessmentsProvided = provided;
        }
    };

    vm.$onInit = () => {
        vm.favouritesKey = mkAssessmentDefinitionsIdsBaseKey(vm.parentEntityRef);
    };

    vm.$onChanges = () => {
        vm.defaultPrimaryList = _
            .chain(vm.assessments)
            .filter(a => a.definition.visibility === "PRIMARY")
            .map(r => r.definition.id)
            .value();

        serviceBroker
            .loadAppData(CORE_API.UserPreferenceStore.findAllForUser,[],  {force: true})
            .then(r => vm.favouriteAssessmentDefnIds = getFavouriteAssessmentDefnIds(vm.favouritesKey, r.data, vm.defaultPrimaryList))
            .then(partitionAssessments);
    };

    vm.toggleFavourite = (assessmentRatingId) => {

        const alreadyFavourite = isFavourite(vm.favouriteAssessmentDefnIds, assessmentRatingId);

        const newFavouritesList = (alreadyFavourite)
            ? _.without(vm.favouriteAssessmentDefnIds, assessmentRatingId)
            : _.concat(vm.favouriteAssessmentDefnIds, assessmentRatingId);

        const message = (alreadyFavourite)? "Removed from favourite assessments" : "Added to favourite assessments";

        serviceBroker
            .execute(
                CORE_API.UserPreferenceStore.saveForUser,
                [{key:  vm.favouritesKey, value: newFavouritesList.toString()}])
            .then(r => vm.favouriteAssessmentDefnIds = getFavouriteAssessmentDefnIds(vm.favouritesKey, r.data, vm.defaultPrimaryList))
            .then(() => partitionAssessments())
            .then(() => toasts.info(message))
            .catch(e => displayError("Could not modify favourite assessment list", e));
    };

    vm.toggleExpandNotProvided = () => {
        vm.expandNotProvided = !vm.expandNotProvided;
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
    id: "waltzAssessmentRatingList"
};

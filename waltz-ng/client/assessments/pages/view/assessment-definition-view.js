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
import {CORE_API} from "../../../common/services/core-api-utils";

import template from "./assessment-definition-view.html";
import {mkEntityLinkGridCell} from "../../../common/grid-utils";


const initialState = {
    editor: "SINGLE",
    canDelete: false,
    currentOrgUnit: null
};

function controller($q,
                    $state,
                    $scope,
                    $stateParams,
                    appStore,
                    notification,
                    serviceBroker,
                    userService) {

    const {definitionId} = $stateParams;
    const vm = Object.assign(this, initialState);
    userService
        .whoami()
        .then(user => vm.user = user);

    const loadAll = () => {
        serviceBroker
            .loadViewData(CORE_API.AssessmentDefinitionStore.getById, [definitionId])
            .then(r => vm.definition = r.data);

        const ratingSchemePromise = serviceBroker
            .loadViewData(CORE_API.RatingSchemeStore.findRatingsSchemeItems, [definitionId])
            .then(r => r.data);

        const ratingPromise = serviceBroker
            .loadViewData(CORE_API.AssessmentRatingStore.findByAssessmentDefinitionId,
                          [definitionId], {force: true})
            .then(r => r.data);

        $q.all([ratingPromise, ratingSchemePromise])
            .then(([ratings, ratingSchemeItems]) => {
                const itemsById = _.keyBy(ratingSchemeItems, "id");
                vm.ratingSchemeItems = ratingSchemeItems;
                vm.appRatings = _.map(ratings, r =>
                    Object.assign(r, {
                        entityRef: r.entityReference,
                        rating: itemsById[r.ratingId],
                        comment: r.comment
                    }));
            });
    };

    loadAll();

    vm.columnDefs = [
        mkEntityLinkGridCell("Entity", "entityRef", "none", "right"),
        {field: "rating.name", name: "Assessment Rating", width: "15%"},
        {field: "comment", name: "Comment"}
    ];

}

controller.$inject = [
    "$q",
    "$state",
    "$scope",
    "$stateParams",
    "ApplicationStore",
    "Notification",
    "ServiceBroker",
    "UserService"
];

const view = {
    template,
    controllerAs: "ctrl",
    controller
};


export default view;
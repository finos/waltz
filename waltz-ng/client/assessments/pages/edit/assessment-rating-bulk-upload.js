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

import template from "./assessment-rating-bulk-upload.html";
import {mkEntityLinkGridCell} from "../../../common/grid-utils";
import {displayError} from "../../../common/error-utils";


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

    console.log($stateParams);
    const {definitionId} = $stateParams;
    const vm = Object.assign(this, initialState);
    userService
        .whoami()
        .then(user => vm.user = user);

    console.log("definition ", definitionId);


    const loadAll = () => {
        serviceBroker
            .loadViewData(CORE_API.AssessmentDefinitionStore.getById, [definitionId])
            .then(r => vm.definition = r.data)
            .then(() => console.log(vm.definition));

        const ratingSchemePromise = serviceBroker
            .loadViewData(CORE_API.RatingSchemeStore.findRatingsSchemeItems, [definitionId])
            .then(r => r.data);

        const ratingPromise = serviceBroker
            .loadViewData(CORE_API.AssessmentRatingStore.findByEntityKindAndAssessmentDefinitionId,
                          ["APPLICATION", definitionId], {force: true})
            .then(r => r.data);

        $q.all([ratingPromise, ratingSchemePromise])
            .then(([ratings, ratingSchemeItems]) => {
                const itemsById = _.keyBy(ratingSchemeItems, "id");
                vm.ratings = _.map(ratings, r => _.extend({rating: itemsById[r.ratingId].name}, r));
                vm.ratingItems = ratingSchemeItems;
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
        mkEntityLinkGridCell("Application", "entityRef", "none", "right"),
        {field: "rating.name", name: "Assessment Rating", width: "20%"},
        {field: "comment", name: "Comment", width: "40%"},
        {
            name: "Operation",
            width: "10%",
            cellTemplate: "<div class=\"ui-grid-cell-contents\"><a ng-click=\"grid.appScope.removeAssessmentRating(row.entity)\" class=\"clickable\">Remove</a></div>"
        }
    ];

    vm.showSingleEditor = () => {
        vm.editor = "SINGLE"
    };

    vm.showBulkEditor = () => {
        vm.editor = "BULK";
    };

    vm.saveRatings = (results) => {
        console.log("Final results", results);

        const unknownIdentifiers = _.chain(results)
            .filter(r => r.action == null)
            .map(r => r.identifier)
            .value();

        const convertToRatingCommand = (r) => Object.assign({}, {
            entityRef: r.entityRef,
            ratingId: r.rating.id,
            operation: r.action,
            comment: r.comment
        });

        const ratingUpdateCommands = _.chain(results)
            .filter(r => r.action === "ADD" || r.action === "UPDATE")
            .map(r => convertToRatingCommand(r))
            .value();

        const ratingRemoveCommands = _.chain(results)
            .filter(r => r.action === "REMOVE")
            .map(r => convertToRatingCommand(r))
            .value();

        if (ratingUpdateCommands.length > 0) {
            serviceBroker
                .execute(CORE_API.AssessmentRatingStore.bulkStore, [definitionId, ratingUpdateCommands])
                .then(() => loadAll())
                .then(() => notification.success(`Added/Updated ratings for ${ratingUpdateCommands.length} applications`))
                .catch(e => displayError(notification, "Failed to save", e));
        }

        if (ratingRemoveCommands.length > 0) {
            serviceBroker
                .execute(CORE_API.AssessmentRatingStore.bulkRemove, [definitionId, ratingRemoveCommands])
                .then(() => loadAll())
                .then(() => notification.success(`Removed ratings for ${ratingRemoveCommands.length} applications`))
                .catch(e => displayError(notification, "Failed to remove assessment ratings", e));
        }

        if (ratingRemoveCommands.length === 0 && ratingUpdateCommands.length === 0){
            notification.info("There are no rating changes found.");
        }
    };

    vm.removeAssessmentRating = (row) => {
        serviceBroker
            .loadViewData(CORE_API.AssessmentRatingStore.remove, [row.entityRef, definitionId])
            .then(r => r.data)
            .then(() => loadAll())
            .then(() => notification.success("Assessment Rating Removed for application " + row.entityRef.name))
            .catch((e) => {
                console.log("WAR: Failed to delete assessment rating for application", {error: e});
                return notification.warning(`Failed to delete assessment rating for application: ${e.data.message}`);
            });
    };

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
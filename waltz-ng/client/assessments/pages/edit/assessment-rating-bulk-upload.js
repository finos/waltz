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
import {initialiseData} from "../../../common";
import toasts from "../../../svelte-stores/toast-store";
import BulkAssessmentRatingSelector from "../../components/bulk-assessment-rating-selector/BulkAssessmentRatingSelector.svelte";

const ratingCellTemplate = `
    <div class="ui-grid-cell-contents">
        <waltz-rating-indicator-cell rating="COL_FIELD"
                                     show-name="true">
        </waltz-rating-indicator-cell>
    </div>`;


const initialState = {
    BulkAssessmentRatingSelector,
    columnDefs: [
        mkEntityLinkGridCell("Entity", "entityRef", "none", "right"),
        {
            field: "rating",
            width: "30%",
            name: "Assessment Rating",
            cellTemplate: ratingCellTemplate,
        },
        {field: "comment", name: "Comment"},
        {
            name: "Operation",
            width: "10%",
            cellTemplate: "<div class=\"ui-grid-cell-contents\"><a ng-click=\"grid.appScope.removeAssessmentRating(row.entity)\" class=\"clickable\">Remove</a></div>"
        }
    ]
};


function controller($q,
                    $stateParams,
                    serviceBroker,
                    userService) {

    const definitionId = $stateParams.id;
    const vm = initialiseData(this, initialState);

    userService
        .whoami()
        .then(user => vm.user = user);

    const loadAll = () => {
        serviceBroker
            .loadViewData(CORE_API.AssessmentDefinitionStore.getById, [definitionId])
            .then(r => {
                vm.definition = r.data
            });

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


    vm.showSingleEditor = () => {
        vm.editor = "SINGLE"
    };

    vm.showBulkEditor = () => {
        vm.editor = "BULK";
    };

    vm.saveRatings = (results) => {
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
                .then(() => toasts.success(`Added/Updated ratings for ${ratingUpdateCommands.length} applications`))
                .catch(e => displayError("Failed to save", e));
        }

        if (ratingRemoveCommands.length > 0) {
            serviceBroker
                .execute(CORE_API.AssessmentRatingStore.bulkRemove, [definitionId, ratingRemoveCommands])
                .then(() => loadAll())
                .then(() => toasts.success(`Removed ratings for ${ratingRemoveCommands.length} applications`))
                .catch(e => displayError("Failed to remove assessment ratings", e));
        }

        if (ratingRemoveCommands.length === 0 && ratingUpdateCommands.length === 0){
            toasts.info("There are no rating changes found.");
        }
    };

    vm.removeAssessmentRating = (row) => {
        if (confirm("Are you sure you want to delete this assessment rating ?")) {
            serviceBroker
                .loadViewData(
                    CORE_API.AssessmentRatingStore.remove,
                    [row.entityRef, definitionId, row.ratingId])
                .then(r => r.data)
                .then(() => loadAll())
                .then(() => toasts.success("Assessment Rating Removed for application " + row.entityRef.name))
                .catch((e) =>
                    displayError(
                        `Failed to delete assessment rating for application: ${e.data.message}`,
                        e));
        }
    };

}

controller.$inject = [
    "$q",
    "$stateParams",
    "ServiceBroker",
    "UserService"
];

const view = {
    template,
    controllerAs: "ctrl",
    controller
};


export default view;
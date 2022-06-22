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

import template from "./assessment-rating-section.html";
import {CORE_API} from "../../../common/services/core-api-utils";
import {mkEnrichedAssessmentDefinitions} from "../../assessment-utils";
import {displayError} from "../../../common/error-utils";
import {resolveResponses} from "../../../common/promise-utils";
import _ from "lodash";
import toasts from "../../../svelte-stores/toast-store";
import AssessmentEditor from "../editor/AssessmentEditor.svelte";

const bindings = {
    parentEntityRef: "<",
};


const initialState = {
    AssessmentEditor,
    permissions: []
};


function controller($q, serviceBroker) {
    const vm = initialiseData(this, initialState);

    const loadAssessments = () => {
        const definitionsPromise = serviceBroker
            .loadViewData(
                CORE_API.AssessmentDefinitionStore.findByKind,
                [vm.parentEntityRef.kind]);

        const ratingsPromise = serviceBroker
            .loadViewData(
                CORE_API.AssessmentRatingStore.findForEntityReference,
                [vm.parentEntityRef],
                {force: true});

        const ratingSchemePromise = serviceBroker
            .loadViewData(
                CORE_API.RatingSchemeStore.findAll);

        return $q
            .all([definitionsPromise, ratingsPromise, ratingSchemePromise])
            .then(responses => {
                [vm.assessmentDefinitions, vm.assessmentRatings, vm.ratingSchemes] = resolveResponses(responses);

                vm.assessments = mkEnrichedAssessmentDefinitions(
                    vm.assessmentDefinitions,
                    vm.ratingSchemes,
                    vm.assessmentRatings);

                if (vm.selectedAssessment) {
                    // re-find the selected assessment
                    vm.selectedAssessment = _.find(
                        vm.assessments,
                        a => a.definition.id === vm.selectedAssessment.definition.id);
                }
            });
    };

    function loadPermissions(definitionId) {
        if (definitionId) {
            serviceBroker
                .loadViewData(
                    CORE_API.AssessmentRatingStore.findRatingPermissions,
                    [vm.parentEntityRef, definitionId],
                    {force: true})
                .then(d => vm.permissions = d.data);
        }
    }

    vm.$onInit = () => {
        loadAssessments();
    };


    // INTERACT

    vm.onSelect = (selectedAssessment) => {
        vm.selectedAssessment = selectedAssessment;
        loadPermissions(selectedAssessment.definition.id);
    };


    vm.onClose = () => {
        vm.selectedAssessment = null;
        loadAssessments();
    };


    vm.onRemove = (definitionId) => {
        return serviceBroker
            .execute(CORE_API.AssessmentRatingStore.remove, [ vm.parentEntityRef, definitionId ])
            .then(() => {
                vm.onClose();
                toasts.warning("Assessment removed");
            })
            .catch(e => {
                displayError("Failed to remove", e);
            });
    };


    vm.onSave = (definitionId, ratingId, comments) => {
        return serviceBroker
            .execute(
                CORE_API.AssessmentRatingStore.store,
                [vm.parentEntityRef, definitionId, ratingId, comments])
            .then(d => {
                loadAssessments();
                toasts.success("Assessment saved");
            })
            .catch(e => displayError("Failed to save", e));
    };

    vm.onLock = (definitionId) => {
        return serviceBroker
            .execute(
                CORE_API.AssessmentRatingStore.lock,
                [vm.parentEntityRef, definitionId])
            .then(d => {
                loadAssessments();
                loadPermissions(definitionId);
                toasts.success("Assessment Locked");
            })
            .catch(e => displayError("Failed to lock", e));
    };

    vm.onUnlock = (definitionId) => {
        return serviceBroker
            .execute(
                CORE_API.AssessmentRatingStore.unlock,
                [vm.parentEntityRef, definitionId])
            .then(d => {
                loadAssessments();
                loadPermissions(definitionId);
                toasts.success("Assessment Unlocked");
            })
            .catch(e => displayError("Failed to unlock", e));
    };

}


controller.$inject = [
    "$q",
    "ServiceBroker"
];


const component = {
    template,
    bindings,
    controller
};


export default {
    component,
    id: "waltzAssessmentRatingSection"
};

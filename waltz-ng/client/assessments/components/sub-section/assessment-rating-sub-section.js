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

import template from "./assessment-rating-sub-section.html";
import {CORE_API} from "../../../common/services/core-api-utils";
import {mkEnrichedAssessmentDefinitions} from "../../assessment-utils";
import {resolveResponses} from "../../../common/promise-utils";


const bindings = {
    parentEntityRef: "<?"
};

const initialState = {
    useExternalEditorPage: false
};


function controller($q, serviceBroker) {
    const vm = initialiseData(this, initialState);

    const loadAll = () => {
        const definitionsPromise = serviceBroker
            .loadViewData(
                CORE_API.AssessmentDefinitionStore.findByEntityReference,
                [vm.parentEntityRef]);

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


    vm.$onChanges = () => {
        if (vm.parentEntityRef) {
            loadAll();
            vm.useExternalEditorPage = _.includes(["CHANGE_UNIT"], vm.parentEntityRef.kind);
        }
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
    id: "waltzAssessmentRatingSubSection"
};

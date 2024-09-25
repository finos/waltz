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

import template from "./recalculate-view.html";
import {CORE_API} from "../common/services/core-api-utils";
import toasts from "../svelte-stores/toast-store";
import {initialiseData} from "../common";
import _ from "lodash";

const initialState = {
    assessmentRippleConfig: []
};


function controller($q, serviceBroker) {
    const vm = initialiseData(this, initialState);

    vm.$onInit = () => {
        const configPromise = serviceBroker
            .loadViewData(CORE_API.AssessmentRatingStore.findRippleConfig);

        const defsPromise = serviceBroker
            .loadViewData(CORE_API.AssessmentDefinitionStore.findAll);

        $q.all([configPromise, defsPromise])
            .then(([configResponse, defsResponse]) => {
                const defsByExtId = _.keyBy(
                    defsResponse.data,
                    d => d.externalId);

                vm.assessmentRippleConfig = _
                    .chain(configResponse.data)
                    .map(d => {
                        const steps = _.map(
                            d.steps,
                            s => ({from: defsByExtId[s.fromDef], to: defsByExtId[s.toDef]}));
                        return Object.assign({}, d, {steps});
                    })
                    .value();
            });
    };

    vm.recalcFlowRatings = () => {
        toasts.info("Flow Ratings recalculation requested");
        serviceBroker
            .execute(CORE_API.FlowClassificationRuleStore.recalculateAll)
            .then(() => toasts.success("Flow Ratings recalculated"));
    };

    vm.recalcDataTypeUsages = () => {
        toasts.info("Data Type Usage recalculation requested");
        serviceBroker
            .execute(CORE_API.DataTypeUsageStore.recalculateAll)
            .then(() => toasts.success("Data Type Usage recalculated"));
    };

    vm.rippleAssessments = () => {
        toasts.info("Assessment Ripple requested");
        serviceBroker
            .execute(CORE_API.AssessmentRatingStore.ripple)
            .then(r => toasts.success(`Assessment Ripple finished. Completed ${r.data} step/s`));
    };
}


controller.$inject = [
    "$q",
    "ServiceBroker"
];


const page = {
    template,
    controller,
    controllerAs: "$ctrl"
};


export default page;
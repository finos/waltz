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

import template from "./picker.html";
import {initialiseData} from "../../common";
import {CORE_API} from "../../common/services/core-api-utils";
import {combinePhysicalWithLogical} from "../../physical-flows/physical-flow-utils";


function convertToChoices(kind, matches, $q, displayNameService, serviceBroker) {

    switch (kind) {
        case "PHYSICAL_FLOW":
            const specIds = _.map(matches, m => m.specificationId);
            const logicalFlowIds = _.map(matches, m => m.logicalFlowId);
            const specPromise = serviceBroker
                .loadViewData(CORE_API.PhysicalSpecificationStore.findByIds, [ specIds ])
                .then(r => r.data);
            const logicalPromise = serviceBroker
                .loadViewData(CORE_API.LogicalFlowStore.findByIds, [ logicalFlowIds ])
                .then(r => r.data);

            return $q.all([specPromise, logicalPromise])
                .then(([specs, logicals]) => {
                    const combined = combinePhysicalWithLogical(matches, logicals, specs, displayNameService);
                    return _.map(combined, c => {
                        const name = `${c.specification.name} - ${c.logical.source.name} -> ${c.logical.target.name}`;
                        return Object.assign({}, c.physical, { name });
                    });
                });
        default:
            return Promise.resolve(_.orderBy(matches, "name"));
    }
}

function controller(matches, $q, $stateParams, displayNameService, serviceBroker) {
    const vm = initialiseData(this, { choices: [] });
    vm.externalId = $stateParams.extId;
    vm.choices = convertToChoices($stateParams.kind, matches, $q, displayNameService, serviceBroker)
        .then(choices => vm.choices = choices);
}


controller.$inject = [
    "matches",
    "$q",
    "$stateParams",
    "DisplayNameService",
    "ServiceBroker"
];

export default {
    controller,
    template,
    controllerAs: "$ctrl",
    scope: {},
    bindToController: true
}
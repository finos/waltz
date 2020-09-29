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

import {ascending} from "d3-array";
import {nest} from "d3-collection";
import {initialiseData} from "../../../common/index";
import {CORE_API} from "../../../common/services/core-api-utils";

import template from "./entity-statistic-section.html";

const bindings = {
    parentEntityRef: "<"
};


const initData = {
    entityStatisticsGrouped: []
};


function controller(serviceBroker) {
    const vm = initialiseData(this, initData);

    vm.$onChanges = (changes) => {
        if (vm.parentEntityRef) {
            serviceBroker
                .loadViewData(CORE_API.EntityStatisticStore.findStatsForEntity, [vm.parentEntityRef])
                .then(result => vm.entityStatisticsGrouped = nest()
                    .key(x => x.definition.category)
                    .sortKeys(ascending)
                    .key(x => x.definition.name)
                    .sortKeys(ascending)
                    .sortValues((a, b) => ascending(a.value.outcome, b.value.outcome))
                    .entries(result.data));
        }
    };
}


controller.$inject = [
    "ServiceBroker"
];


export default {
    component: {
        template,
        bindings,
        controller
    },
    id: "waltzEntityStatisticSection"
};

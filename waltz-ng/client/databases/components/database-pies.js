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

import { environmentColorScale, variableScale } from "../../common/colors";
import { notEmpty, toKeyCounts } from "../../common";
import { endOfLifeStatus } from "../../common/services/enums/end-of-life-status";
import template from "./database-pies.html";


const bindings = {
    databases: "<"
};




const PIE_SIZE = 70;


function prepareStats(databases = []) {

    const environment = toKeyCounts(databases, d => d.environment);
    const vendor = toKeyCounts(databases, d => d.dbmsVendor);
    const endOfLifeStatus = toKeyCounts(databases, d => d.endOfLifeStatus);

    return {
        environment,
        vendor,
        endOfLifeStatus
    };
}


function controller($scope) {

    const vm = this;

    vm.pieConfig = {
        environment: {
            size: PIE_SIZE,
            colorProvider: (d) => environmentColorScale(d.key)
        },
        vendor: {
            size: PIE_SIZE,
            colorProvider: (d) => variableScale(d.key)
        },
        endOfLifeStatus: {
            size: PIE_SIZE,
            colorProvider: (d) => variableScale(d.key),
            labelProvider: (d) => endOfLifeStatus[d.key] ? endOfLifeStatus[d.key].name : "Unknown"
        }
    };

    const recalcPieData = (databases = []) => {
        if (notEmpty(databases)) {
            vm.pieData = prepareStats(databases);
        }
    };


    vm.$onChanges = () => {
        if(vm.databases) {
            recalcPieData(vm.databases);
        }
    };

}

controller.$inject = [ "$scope" ];


const component = {
    bindings,
    template,
    controller
};


export default component;

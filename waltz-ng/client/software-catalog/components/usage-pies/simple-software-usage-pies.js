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
import {maturityColorScale, variableScale} from "../../../common/colors";
import template from "./simple-software-usage-pies.html";


const bindings = {
    usages: "<",
    packages: "<"
};


const PIE_SIZE = 70;


function prepareStats(items = [], usages = []) {
    const usageCounts = _.countBy(usages, "softwarePackageId");

    const countPieDataBy = (items = [], fn = (x => x)) =>
        _.chain(items)
            .groupBy(fn)
            .map((group, key) => {
                const calculatedCount = _.reduce(
                    group,
                    (acc, groupItem) => acc + usageCounts[groupItem.id] || 1,
                    0);
                return {
                    key,
                    count: calculatedCount
                };
            })
            .value();


    return {
        maturity: countPieDataBy(items, item => item.maturityStatus),
        vendor: countPieDataBy(items, item => item.vendor)
    };
}


function controller() {

    const vm = this;

    vm.pieConfig = {
        maturity: {
            size: PIE_SIZE,
            colorProvider: (d) => maturityColorScale(d.key)
        },
        vendor: {
            size: PIE_SIZE,
            colorProvider: (d) => variableScale(d.key)
        }
    };


    const recalcPieData = () => {
        vm.pieData = prepareStats(vm.packages, vm.usages);
    };


    vm.$onChanges = () => {
        if(vm.packages && vm.usages) {
            recalcPieData();
        }
    }

}


controller.$inject = [ ];


const component = {
    bindings,
    template,
    controller
};


export default {
    component,
    id: "waltzSimpleSoftwareUsagePies"
};

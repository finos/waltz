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
import { variableScale } from "../../common/colors";
import { mkSummaryTableHeadings, navigateToStatistic } from "../utilities";
import template from "./entity-statistic-summary-card.html";

const bindings = {
    definition: "<",
    parentRef: "<",
    subTitle: "@",
    summary: "<"
};


const PIE_SIZE = 100;


function mkStatChartData(counts = []) {
    return {
        config: {
            colorProvider: (d) => variableScale(d.key),
            labelProvider: d => d.key,
            size: PIE_SIZE
        },
        data: _.chain(counts)
            .map(c => ({
                key: c.id,
                count: c.count
            }))
            .value()
    };
}


function controller($state) {

    const vm = this;

    vm.$onChanges = () => {
        const tallies = vm.summary
            ? vm.summary.tallies
            : [];
        vm.pie = mkStatChartData(tallies);
        vm.tableHeadings = mkSummaryTableHeadings(vm.definition);
    };

    vm.goToStatistic = (definition) => {
        navigateToStatistic($state, definition.id, vm.parentRef);
    };
}


controller.$inject = [
    "$state"
];


const component = {
    bindings,
    controller,
    template
};


export default component;
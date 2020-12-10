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
import { mkSummaryTableHeadings } from "../utilities";
import template from "./entity-statistic-detail-panel.html";


const bindings = {
    applications: "<",
    definition: "<",
    orgUnits: "<",
    summary: "<",
    values: "<"
};


const PIE_SIZE = 140;


function mkStatChartData(counts = [], onSelect) {
    return {
        config: {
            colorProvider: (d) => variableScale(d.key),
            labelProvider: d => d.key,
            onSelect,
            size: PIE_SIZE
        },
        data: _.chain(counts)
            .map(c => ({ key: c.id, count: c.count }))
            .value()
    };
}


function controller() {
    const vm = this;

    const pieClickHandler = d => {
        vm.selectedPieSegment = d;
    };

    vm.$onChanges = () => {
        vm.pie = mkStatChartData(
            vm.summary ? vm.summary.tallies : [],
            pieClickHandler);

        vm.tableHeadings = mkSummaryTableHeadings(vm.definition);
        vm.lastUpdatedAt = vm.summary ? vm.summary.lastUpdatedAt : "";
    }
}


const component = {
    bindings,
    controller,
    template
};


export default component;
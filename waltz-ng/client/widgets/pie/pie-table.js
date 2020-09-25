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

import template from "./pie-table.html";

import { toSegments } from "./pie-utils";
import { initialiseData } from "../../common/index";
import { invokeFunction } from "../../common";


const bindings = {
    data: "<",
    config: "<",
    name: "@",
    subName: "@",
    icon: "@",
    description: "@",
    selectedSegmentKey: "<",
    renderMode: "@?"
};


const initialState = {
};


function controller() {

    const vm = initialiseData(this, initialState);

    const dataChanged = (data = []) => {
        const segmentedData = toSegments(data);
        vm.summarizedSegments = _.chain(segmentedData.primary).concat([segmentedData.overspillSummary]).compact().value();
        vm.detailedSegments = _.chain(segmentedData.primary).concat(segmentedData.overspill).value();
        vm.pieData = vm.summarizedSegments;
        vm.tableData = vm.summarizedSegments;
    };

    function pieOnSelect(d) {
        if (d.isOverspillSummary === true || vm.config.onSelect == null) {
            vm.selectedSegmentKey = d.key;
        } else {
            invokeFunction(vm.config.onSelect, d);
        }
    }


    function tableOnSelect(d) {
        const handleNormalSelection = (d) => {
            if (_.isNil(vm.config.onSelect)) {
                vm.selectedSegmentKey = d ? d.key : null;
            } else {
                vm.config.onSelect(d);
            }
        };

        const handleExpansionOfOtherSegment = () => {
            vm.tableData = vm.detailedSegments;
        };

        if (d != null && d.isOverspillSummary === true) {
            handleExpansionOfOtherSegment();
        } else {
            handleNormalSelection(d);
        }
    }


    vm.$onChanges = (changes) => {
        if (changes.data) {
            dataChanged(vm.data);
        }

        if (changes.config) {
            vm.pieConfig = Object.assign(
                {},
                vm.config,
                { onSelect: pieOnSelect });
            vm.tableConfig = Object.assign(
                {},
                vm.config,
                { onSelect: tableOnSelect });
        }
    };

    vm.toDisplayName = (k) => vm.config.labelProvider
        ? vm.config.labelProvider(k)
        : k;
}


const component = {
    template,
    bindings,
    controller
};


export default component;

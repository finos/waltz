/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017, 2018, 2019 Waltz open source project
 * See README.md for more information
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import template from "./pie-table.html";

import {toSegments} from "./pie-utils";
import {initialiseData} from "../../common/index";
import {invokeFunction} from "../../common";


const bindings = {
    data: "<",
    config: "<",
    title: "@",
    subTitle: "@",
    icon: "@",
    description: "@",
    selectedSegmentKey: "<",
    renderMode: "@?"
};


const initialState = {
};


function controller() {

    const vm = initialiseData(this, initialState);

    const defaultOnSelect = (d) => {
        vm.selectedSegmentKey = d
            ? d.key
            : null;
    };

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
        if (d != null && (vm.config.onSelect == null || d.isOverspillSummary === true )) {
            vm.selectedSegmentKey = d.key;
            vm.tableData = vm.detailedSegments;
        } else {
            invokeFunction(vm.config.onSelect, d);
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

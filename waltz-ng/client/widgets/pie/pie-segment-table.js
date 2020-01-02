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
import { numberFormatter, toPercentage } from "../../common/string-utils";
import template from "./pie-segment-table.html";


const bindings = {
    config: "<",
    data: "<",
    headings: "<",
    selectedSegmentKey: "<"
};


const initialState = {
    total: 0,
    headings: []
};


const defaultConfig = {
    labelProvider: (d) => d.key,
    valueProvider: (d) => d.count,
    colorProvider: (d) => variableScale(d.key),
    descriptionProvider: (d) => null
};


const defaultOnSelect = (d) => console.log("no pie-segment-table::on-select handler provided:", d);


function controller() {
    const vm = _.defaultsDeep(this, initialState);

    vm.$onChanges = (changes) => {
        if (changes.data || changes.config) {
            vm.config = _.defaultsDeep(vm.config, defaultConfig);
            vm.total = _.sumBy(vm.data, vm.config.valueProvider);
            vm.data = _.map(
                vm.data,
                d => Object.assign(
                    {},
                    d,
                    { percentage: toPercentage(vm.config.valueProvider(d), vm.total) }));
            vm.totalStr = numberFormatter(vm.total, 2, false);
            vm.rowSelected = (d, e) => {
                e.stopPropagation();
                (vm.config.onSelect || defaultOnSelect)(d);
            };
        }
    };
}


const component = {
    bindings,
    controller,
    template
};


export default component;
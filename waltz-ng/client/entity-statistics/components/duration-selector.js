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
import {initialiseData} from "../../common";
import template from './duration-selector.html';


const bindings = {
    duration: '<',
    onChange: '<'
};


const durations = [
    { code: 'WEEK', name: "1w" },
    { code: 'MONTH', name: "1m" },
    { code: 'QUARTER', name: "3m" },
    { code: 'HALF_YEAR', name: "6m" },
    { code: 'YEAR', name: "1y" }
];


function mkOptions(active) {
    return _.map(
        durations,
        d => Object.assign(
            {},
            d,
            { active: d.code === active }));
}


const initialState = {
    selection: 'MONTH',
    options: mkOptions('MONTH'),
    onChange: (d) => console.log('duration-selector: onChange: ', d)
};




function controller() {
    const vm = initialiseData(this, initialState);

    vm.$onChanges = () => vm.options = mkOptions(vm.duration);
    vm.onClick = (d) => vm.onChange(d.code);
}


controller.$inject = [];


const component = {
    template,
    bindings,
    controller
};


export default component;

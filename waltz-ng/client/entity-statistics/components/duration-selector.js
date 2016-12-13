/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016  Khartec Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import _ from "lodash";
import {initialiseData} from "../../common";


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


const template = require('./duration-selector.html');


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

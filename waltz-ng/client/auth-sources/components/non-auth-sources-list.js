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
    dataTypes: '<',
    distributors: '<',
    logicalFlows: '<',
};


const initialState = {
    nonAuthSources: [],
    distributors: [],
};


const template = require('./non-auth-sources-list.html');


function calculate(dataTypes = [], distributorsByDataType = [], logicalFlows = []) {
    const logicalFlowsBySource = _.groupBy(logicalFlows, 'source.id');
    const dataTypesById = _.keyBy(dataTypes, 'id');

    const nonAuthSources = _.chain(distributorsByDataType)
        .flatMap((values, key) => {
            const dataType = dataTypesById[key] || {};
            return  _.map(values, v => Object.assign(v, {dataType} ));
        })
        .map(distributor => {
            const consumers = _.map(logicalFlowsBySource[distributor.id] || [], f => f.target);
            return Object.assign({}, distributor, {consumers});
        })
        .value();

    return {
        nonAuthSources
    };
}


function controller() {
    const vm = initialiseData(this, initialState);

    vm.$onChanges = changes => {
        const nonAuthSources = calculate(
            vm.dataTypes,
            vm.distributors,
            vm.logicalFlows);
        Object.assign(vm, nonAuthSources);
    };


    vm.showDetail = selected =>
        vm.selected = selected;
}


controller.$inject = [];


const component = {
    bindings,
    controller,
    template
};


export default component;
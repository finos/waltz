/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016  Khartec Ltd.
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

import _ from 'lodash';


const bindings = {
    flowData: '<',
    applications: '<',
    onLoadDetail: '<'
};


const initialState = {
    export: () => console.log('lfts: default do-nothing export function'),
    flowData: null,
    applications: [],
    onLoadDetail: () => console.log('onLoadDetail not provided to logical flows tabgroup section'),
    visibility: {
        exportButton: false,
        sourcesOverlay: false
    }
};


function calcHasFlows(flowData) {
    const counts = _.get(flowData, 'stats.flowCounts') || {}
    const total = _.sum(_.values(counts));
    return total > 0;
}


function controller() {
    const vm = _.defaultsDeep(this, initialState);

    vm.tabChanged = (name, index) => {
        vm.visibility.flowConfigButton = index > 0;
        vm.visibility.exportButton = index == 2;
        if(index === 0) vm.visibility.flowConfigOverlay = false;
    };

    vm.tableInitialised = (cfg) =>
        vm.export = () => cfg.exportFn('logical-flows.csv');

    vm.$onChanges = c => {
        vm.hasFlows = calcHasFlows(vm.flowData);
    }
}


controller.$inject = [
];


const component = {
    controller,
    bindings,
    template: require('./logical-flows-tabgroup-section.html')
};


export default component;
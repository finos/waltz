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
import _ from "lodash";
import {enrichServerStats} from "../../../server-info/services/server-utilities";
import {calcPortfolioCost} from "../../../asset-cost/services/asset-cost-utilities";
import {calcComplexitySummary} from "../../../complexity/services/complexity-utilities";


const bindings = {
    applications: '<',
    children: '<',
    complexity: '<',
    logicalFlows: '<',
    measurable: '<',
    parents: '<',
    serverStats: '<',
    totalCost: '<'
};


const template = require('./measurable-summary.html');


function enrichWithRefs(measurables = []) {
    return _.map(
        measurables,
        d => Object.assign(
            {},
            d,
            { entityReference: {kind: 'MEASURABLE', id: d.id, name: d.name, description: d.description }}));
}


function prepareChildRefs(children = [], measurable = {}) {
    const cs = _.chain(children)
        .filter(c => c.id != measurable.id)  // not self
        .filter(c => c.parentId == measurable.id) // only immediate children
        .value();
    return enrichWithRefs(cs);
}


function prepareParentRefs(parents = [], measurable = {}) {
    const ps = _.filter(parents, p => p.id != measurable.id)  // not self
    return enrichWithRefs(ps);
}


function controller() {
    const vm = this;

    vm.$onChanges = (c) => {
        if (c.totalCost) vm.portfolioCostStr = calcPortfolioCost(vm.totalCost);
        if (c.complexity) vm.complexitySummary = calcComplexitySummary(vm.complexity);
        if (c.serverStats) vm.enrichedServerStats = enrichServerStats(vm.serverStats);
        if (c.children || c.measurable) vm.childRefs = prepareChildRefs(vm.children, vm.measurable);
        if (c.parents || c.measurable) vm.parentRefs = prepareParentRefs(vm.parents, vm.measurable);

        if (vm.measurable) {
            vm.parentEntityRef = {
                kind: 'MEASURABLE',
                id: vm.measurable.id
            };
        }
    };
}

const component = {
    template,
    bindings,
    controller
};

export default component;
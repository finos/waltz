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

function controller() {
    const vm = this;

    vm.$onChanges = (c) => {
        if (c.totalCost) vm.portfolioCostStr = calcPortfolioCost(vm.totalCost);
        if (c.complexity) vm.complexitySummary = calcComplexitySummary(vm.complexity);
        if (c.serverStats) vm.enrichedServerStats = enrichServerStats(vm.serverStats);
    };
}

const component = {
    template,
    bindings,
    controller
};

export default component;
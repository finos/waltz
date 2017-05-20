
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
import {buildHierarchies} from "../../../common/hierarchy-utils";


const bindings = {
    orgUnitDescendants: '<',
    apps: '<',
    complexity: '<',
    totalCost: '<',
    immediateHierarchy: '<',
    flows: '<',
    serverStats: '<',
    orgUnit: '<',
    loadOrgUnitDescendants: '<'
};


const template = require('./org-unit-overview.html');


function buildTree(orgUnits = [], self = {}) {
    return buildHierarchies(_.filter(orgUnits, ou => ou.id != self.id));
}


function calcParentsAndChildren(hierarchy = [], orgUnit) {
    if (! orgUnit) return { parentOrgUnits: [], childOrgUnits: [] };
    const self = _.find(hierarchy, { entityReference: { id: orgUnit.id } });
    const parentOrgUnits = self
        ? _.filter(hierarchy, h => h.level < self.level)
        : [];

    const childOrgUnits = self
        ? _.filter(hierarchy, h => h.level > self.level)
        : [];

    return { parentOrgUnits, childOrgUnits };
}


function controller() {
    const vm = this;

    vm.$onChanges = () => {
        Object.assign(vm, calcParentsAndChildren(vm.immediateHierarchy, vm.orgUnit));
        vm.complexitySummary = calcComplexitySummary(vm.complexity);
        vm.portfolioCostStr = calcPortfolioCost(vm.totalCost);
        vm.enrichedServerStats = enrichServerStats(vm.serverStats);
        vm.descendantsTree = buildTree(vm.orgUnitDescendants, vm.orgUnit);

        if (vm.orgUnit) {
            vm.entityRef = {
                kind: 'ORG_UNIT',
                id: vm.orgUnit.id
            };
        }
    };
}


controller.$inject = [];


const component = {
    bindings,
    template,
    controller
};


export default component;

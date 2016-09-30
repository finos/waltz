
/*
 *  Waltz
 * Copyright (c) David Watkins. All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 *
 */
import _ from "lodash";
import {enrichServerStats} from "../../../server-info/services/server-utilities";
import {calcPortfolioCost} from "../../../asset-cost/services/asset-cost-utilities";
import {calcComplexitySummary} from "../../../complexity/services/complexity-utilities";
import {buildHierarchies} from "../../../common";


const bindings = {
    orgUnitDescendants: '<',
    apps: '<',
    complexity: '<',
    costs: '<',
    immediateHierarchy: '<',
    flows: '<',
    ratings: '<',
    serverStats: '<',
    orgUnit: '<',
    loadOrgUnitDescendants: '<'
};


const template = require('./org-unit-overview.html');



function calcCapabilityStats(ratings = []) {
    const caps = _.chain(ratings)
        .uniqBy(c => c.capabilityId)
        .value();

    const appCount = _.chain(ratings)
        .map('parent.id')
        .uniq()
        .value()
        .length;

    return {
        total: caps.length,
        perApplication: appCount > 0
            ? Number(caps.length / appCount).toFixed(1)
            : '-'
    };
}


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
        vm.portfolioCostStr = calcPortfolioCost(vm.costs);
        vm.enrichedServerStats = enrichServerStats(vm.serverStats);
        vm.descendantsTree = buildTree(vm.orgUnitDescendants, vm.orgUnit);
        vm.capabilityStats = calcCapabilityStats(vm.ratings);
    };
}


controller.$inject = [];


const component = {
    bindings,
    template,
    controller
};


export default component;

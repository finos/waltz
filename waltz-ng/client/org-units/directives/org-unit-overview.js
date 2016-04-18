
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

import { enrichServerStats } from "../../server-info/services/server-utilities";
import { calcPortfolioCost } from "../../asset-cost/services/asset-cost-utilities";
import { calcComplexitySummary } from "../../complexity/services/complexity-utilities";
import { buildHierarchies, findNode, getParents } from "../../common";


const BINDINGS = {
    unitId: '=',
    allUnits: '=',
    apps: '=',
    flows: '=',
    ratings: '=',
    costs: '=',
    orgServerStats: '=',
    complexity: '='
};



function calcCapabilityStats(ratings) {
    const caps = _.chain(ratings)
        .map("capability")
        .uniqBy(c => c.id)
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


function controller($scope, orgUnitStore) {
    const vm = this;
    vm.saveDescriptionFn = (newValue, oldValue) =>
            orgUnitStore.updateDescription(vm.unit.id, newValue, oldValue);

    $scope.$watch(
        'ctrl.ratings',
        ratings => {
            if (!ratings) return;
            vm.capabilityStats = calcCapabilityStats(ratings);
        });

    $scope.$watch(
        'ctrl.allUnits',
        (units) => {
            const roots = buildHierarchies(units);
            const node = findNode(roots, vm.unitId);
            vm.unit = node;
            vm.parents = getParents(node);
        });

    $scope.$watch(
        'ctrl.costs',
        cs => vm.portfolioCostStr = calcPortfolioCost(cs));

    $scope.$watch(
        'ctrl.orgServerStats',
        stats => {
            if (!stats) return;
            const serverStats = _.reduce(
                stats,
                (acc, stat) => {
                    const virtualCount = acc.virtualCount + stat.virtualCount;
                    const physicalCount = acc.physicalCount + stat.physicalCount;
                    return { virtualCount, physicalCount };
                },
                { virtualCount: 0, physicalCount: 0});

            enrichServerStats(serverStats);

            vm.serverStats = serverStats;
        });

    $scope.$watch(
        'ctrl.complexity',
        cs => vm.complexitySummary = calcComplexitySummary(cs));
}

controller.$inject = ['$scope', 'OrgUnitStore'];


export default () => ({
    restrict: 'E',
    replace: true,
    template: require('./org-unit-overview.html'),
    scope: {},
    bindToController: BINDINGS,
    controllerAs: 'ctrl',
    controller
});


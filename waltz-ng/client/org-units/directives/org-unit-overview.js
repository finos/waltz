
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

import _ from 'lodash';
import numeral from 'numeral';

import { lifecyclePhaseColorScale, capabilityColorScale, flowDirectionColorScale } from '../../common/colors';


function calcCapabilityPieStats(capabilityRatings) {
    return _.chain(capabilityRatings)
            .countBy('ragRating')
            .map((v, k) => ({ key: k, count: v }))
            .value();
}


function calcAppPieStats(apps) {
    return _.chain(apps)
            .countBy('lifecyclePhase')
            .map((v, k) => ({ key: k, count: v }))
            .value();
}


function calcAppConnectionPieStats(flows, apps) {
    const logicalFlows = flows.flows;

    const orgMemberAppIds = _.map(apps, 'id');

    return _.chain(logicalFlows)
        .uniq(false, f => f.source.id + '.' + f.target.id)
        .map(f => {
            const sourceIsMember = _.contains(orgMemberAppIds, f.source.id);
            const targetIsMember = _.contains(orgMemberAppIds, f.target.id);
            if (sourceIsMember && targetIsMember) return 'INTRA';
            if (sourceIsMember) return 'INBOUND';
            if (targetIsMember) return 'OUTBOUND';
            return 'UNKNOWN';
        })
        .countBy()
        .map((v, k) => ({ key: k, count: v }))
        .value();
}


function calcCapabilityStats(ratings) {
    const caps = _.chain(ratings)
        .map("capability")
        .uniq(false, c => c.id)
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

    const pies = {
        app: {
            config: {
                colorProvider: (d) => lifecyclePhaseColorScale(d.data.key),
                size: 80
            }
        },
        capability: {
            config: {
                colorProvider: (d) => capabilityColorScale(d.data.key),
                size: 80
            }
        },
        appConnections: {
            config: {
                colorProvider: (d) => flowDirectionColorScale(d.data.key),
                size: 80
            }
        }
    };

    vm.pies = pies;

    $scope.$watch('ctrl.apps', apps => {
        if (!apps) return;
        vm.pies.app.data = calcAppPieStats(apps);
    });

    $scope.$watch('ctrl.ratings', ratings => {
        if (!ratings) return;
        vm.pies.capability.data = calcCapabilityPieStats(ratings);
        vm.capabilityStats = calcCapabilityStats(ratings);
    });

    $scope.$watch('ctrl.flows', flows => {
        if (!flows) return;
        vm.pies.appConnections.data = calcAppConnectionPieStats(flows, this.apps);
    });

    $scope.$watch('ctrl.costs', costs => {
        if (!costs) return;
        const amount = _.sum(costs, 'cost.amount');

        vm.portfolioCostStr = '€ ' + nFormatter(amount, 1);
    });

    $scope.$watch('ctrl.orgServerStats', stats => {
        if (!stats) return;
        const serverStats = _.foldl(
            stats,
            (acc, stat) => {
                const total = acc.total + stat.virtualCount + stat.physicalCount;
                const virtual = acc.virtual + stat.virtualCount;
                const physical = acc.physical + stat.physicalCount;
                return { total, virtual, physical };
            },
            { total: 0, virtual: 0, physical: 0});

        serverStats.virtualPercentage = serverStats.total > 0
                ? Number((serverStats.virtual / serverStats.total) * 100).toFixed(1)
                : "-";

        vm.serverStats = serverStats;
    })



}

function nFormatter(num, digits) {
    var si = [
        { value: 1E12, symbol: "T" },
        { value: 1E9,  symbol: "B" },
        { value: 1E6,  symbol: "M" },
        { value: 1E3,  symbol: "k" }
    ], i;
    for (i = 0; i < si.length; i++) {
        if (num >= si[i].value) {
            return (num / si[i].value).toFixed(digits).replace(/\.?0+$/, "") + si[i].symbol;
        }
    }
    return num;
}

controller.$inject = ['$scope', 'OrgUnitStore'];

export default () => ({
    restrict: 'E',
    replace: true,
    template: require('./org-unit-overview.html'),
    scope: {
        unit: '=',
        parent: '=',
        children: '=',
        apps: '=',
        flows: '=',
        ratings: '=',
        costs: '=',
        orgServerStats: '='
    },
    bindToController: true,
    controllerAs: 'ctrl',
    controller
});


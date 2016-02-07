
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

import { numberFormatter } from '../../common';

import {
    lifecyclePhaseColorScale,
    capabilityColorScale,
    flowDirectionColorScale,
    ragColorScale } from '../../common/colors';



function calcAppPhasePieStats(apps) {
    return _.chain(apps)
            .countBy('lifecyclePhase')
            .map((v, k) => ({ key: k, count: v }))
            .value();
}


function calcAppInvestmentPieStats(apps) {
    return _.chain(apps)
            .countBy('overallRating')
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

    const investmentLabels = {
        'R' : 'Disinvest',
        'A' : 'Maintain',
        'G' : 'Invest'
    };

    const pies = {
        appPhase: {
            config: {
                colorProvider: (d) => lifecyclePhaseColorScale(d.data.key),
                size: 80
            }
        },
        appConnections: {
            config: {
                colorProvider: (d) => flowDirectionColorScale(d.data.key),
                size: 80
            }
        },
        appInvestment: {
            config: {
                colorProvider: (d) => ragColorScale(d.data.key),
                size: 80,
                labelProvider: (k) => investmentLabels[k] || 'Unknown'
            }
        }
    };

    vm.pies = pies;

    $scope.$watch('ctrl.apps', apps => {
        if (!apps) return;
        vm.pies.appPhase.data = calcAppPhasePieStats(apps);
        vm.pies.appInvestment.data = calcAppInvestmentPieStats(apps);
    });

    $scope.$watch('ctrl.ratings', ratings => {
        if (!ratings) return;
        vm.capabilityStats = calcCapabilityStats(ratings);
    });

    $scope.$watch('ctrl.flows', flows => {
        if (!flows) return;
        vm.pies.appConnections.data = calcAppConnectionPieStats(flows, this.apps);
    });

    $scope.$watch('ctrl.costs', costs => {
        if (!costs) return;
        const amount = _.sum(costs, 'cost.amount');

        vm.portfolioCostStr = '€ ' + numberFormatter(amount, 1);
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
    });

    $scope.$watch('ctrl.complexity', complexity => {
        if (!complexity) return;
        const cumulativeScore = _.sum(complexity, "overallScore");
        const averageScore = complexity.length > 0 ? cumulativeScore / complexity.length : 0;
        vm.complexitySummary =  {
            cumulativeScore,
            averageScore
        };
    })



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
        orgServerStats: '=',
        complexity: '='
    },
    bindToController: true,
    controllerAs: 'ctrl',
    controller
});


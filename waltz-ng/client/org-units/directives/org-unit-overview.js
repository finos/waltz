
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

import { lifecyclePhaseColorScale, capabilityColorScale, flowDirectionColorScale } from '../../common/colors';

import _ from 'lodash';

function calcCapabilityStats(capabilityRatings) {
    return _.chain(capabilityRatings)
            .countBy('ragRating')
            .map((v, k) => ({ key: k, count: v }))
            .value();
}


function calcAppStats(apps) {
    return _.chain(apps)
            .countBy('lifecyclePhase')
            .map((v, k) => ({ key: k, count: v }))
            .value();
}


function calcFlowStats(flows, apps) {
    const logicalFlows = flows.flows;

    const appIds = _.map(apps, 'id');

    return _.chain(logicalFlows)
        .map(f => {
            const sourceIsMember = _.contains(appIds, f.source.id);
            const targetIsMember = _.contains(appIds, f.target.id);
            if (sourceIsMember && targetIsMember) return 'INTRA';
            if (sourceIsMember) return 'INBOUND';
            if (targetIsMember) return 'OUTBOUND';
            return 'UNKNOWN';
        })
        .countBy()
        .map((v, k) => ({ key: k, count: v }))
        .value();
}


function controller($scope, orgUnitStore) {
    const vm = this;
    vm.saveDescriptionFn = (newValue, oldValue) =>
            orgUnitStore.updateDescription(vm.unit.id, newValue, oldValue);

    const pies = {
        app: {
            config: {
                colorProvider: (d) => lifecyclePhaseColorScale(d.data.key),
                size: 100
            }
        },
        capability: {
            config: {
                colorProvider: (d) => capabilityColorScale(d.data.key),
                size: 100
            }
        },
        flows: {
            config: {
                colorProvider: (d) => flowDirectionColorScale(d.data.key),
                size: 100
            }
        }
    };

    vm.pies = pies;

    $scope.$watch('ctrl.apps', apps => {
        if (!apps) return;
        vm.pies.app.data = calcAppStats(apps);
    });

    $scope.$watch('ctrl.ratings', ratings => {
        if (!ratings) return;
        vm.pies.capability.data = calcCapabilityStats(ratings);
    });

    $scope.$watch('ctrl.flows', flows => {
        if (!flows) return;
        vm.pies.flows.data = calcFlowStats(flows, this.apps);
    });

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
        ratings: '='
    },
    bindToController: true,
    controllerAs: 'ctrl',
    controller
});


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
import {lifecyclePhaseColorScale, variableScale} from "../common/colors";
import {tallyBy} from "../common/tally-utils";

const PIE_SIZE = 70;

function hasInvolvements(involvements) {
    return involvements.all.length > 0;
}


function mkChartData(data, groupingField, size, colorProvider) {
    return {
        config: {
            colorProvider: (d) => colorProvider(d.data.key),
            size
        },
        data: tallyBy(data, groupingField)
    };
}


function controller($scope,
                    $stateParams,
                    viewService,
                    historyStore) {

    const vm = this;
    vm.state = viewService.state;

    const employeeId = $stateParams.empId;

    viewService
        .load(employeeId);


    $scope.$watch(() => viewService.state.model, () => {
        const model = viewService.state.model;
        Object.assign(vm, model);

        if (model.person) {
            historyStore.put(model.person.displayName, 'PERSON', 'main.person.view', { empId: model.person.employeeId });
        }

        vm.hasAppInvolvements = hasInvolvements(model.appInvolvements);
        vm.hasEndUserAppInvolvements = hasInvolvements(model.endUserAppInvolvements);
        vm.hasInvolvements = vm.hasAppInvolvements || vm.hasEndUserAppInvolvements;
        vm.charts = {
            apps: {
                byLifecyclePhase: mkChartData(model.appInvolvements.all, 'lifecyclePhase', PIE_SIZE, lifecyclePhaseColorScale),
                byKind: mkChartData(model.appInvolvements.all, 'kind', PIE_SIZE, variableScale)
            },
            endUserApps: {
                byLifecyclePhase: mkChartData(model.endUserAppInvolvements.all, 'lifecyclePhase', PIE_SIZE, lifecyclePhaseColorScale),
                byKind: mkChartData(model.endUserAppInvolvements.all, 'kind', PIE_SIZE, variableScale),
                byRiskRating: mkChartData(model.endUserAppInvolvements.all, 'riskRating', PIE_SIZE, variableScale)
            }
        };

    }, true);


    vm.onAssetBucketSelect = bucket => {
        $scope.$applyAsync(() => {
            viewService.selectAssetBucket(bucket);
        });
    };

    vm.loadFlowDetail = () => viewService.loadFlowDetail();


}

controller.$inject = [
    '$scope',
    '$stateParams',
    'PersonViewDataService',
    'HistoryStore'
];


export default {
    template: require('./person-view.html'),
    controller,
    controllerAs: 'ctrl'
};

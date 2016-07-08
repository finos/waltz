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
import {lifecyclePhaseColorScale, variableScale} from "../common/colors";

const PIE_SIZE = 70;

function hasInvolvements(involvements) {
    return involvements.all.length > 0;
}


function mkAppChartData(apps) {

    const byLifecyclePhase = {
        config: {
            colorProvider: (d) => lifecyclePhaseColorScale(d.data.key),
            size: PIE_SIZE
        },
        data: _.chain(apps)
            .countBy('lifecyclePhase')
            .map((v, k) => ({ key: k, count: v }))
            .value()
    };

    const byKind = {
        config: {
            colorProvider: (d) => variableScale(d.data.key),
            size: PIE_SIZE
        },
        data: _.chain(apps)
            .countBy('kind')
            .map((v, k) => ({ key: k, count: v }))
            .value()
    };

    return {
        byLifecyclePhase,
        byKind
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
            apps: mkAppChartData(model.appInvolvements.all),
            endUserApps: mkAppChartData(model.endUserAppInvolvements.all)
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

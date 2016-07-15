
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
import EventDispatcher from "../common/EventDispatcher";
import {lifecyclePhaseColorScale, variableScale} from "../common/colors";
import {tallyBy} from "../common/tally-utils";
import {riskRatingDisplayNames} from "../common/services/display_names";


const PIE_SIZE = 70;


function mkChartData(data, groupingField, size, colorProvider = variableScale, labelProvider = null) {
    return {
        config: {
            colorProvider: (d) => colorProvider(d.data.key),
            labelProvider,
            size
        },
        data: tallyBy(data, groupingField)
    };
}


function controller($stateParams,
                    $scope,
                    viewDataService,
                    viewOptions,
                    historyStore) {

    const id = $stateParams.id;
    const vm = this;

    const refresh = () => {
        if (!vm.rawViewData) return;
        const orgUnit = vm.rawViewData.orgUnit;

        historyStore.put(orgUnit.name, 'ORG_UNIT', 'main.org-unit.view', { id: orgUnit.id });

        vm.viewData = viewOptions.filter(vm.rawViewData);
        vm.appSummaryCharts = {
            apps: {
                byLifecyclePhase: mkChartData(vm.viewData.apps, 'lifecyclePhase', PIE_SIZE, lifecyclePhaseColorScale),
                byKind: mkChartData(vm.viewData.apps, 'kind', PIE_SIZE, variableScale)
            },
            endUserApps: {
                byLifecyclePhase: mkChartData(vm.viewData.endUserApps, 'lifecyclePhase', PIE_SIZE, lifecyclePhaseColorScale),
                byKind: mkChartData(vm.viewData.endUserApps, 'platform', PIE_SIZE, variableScale),
                byRiskRating: mkChartData(vm.viewData.endUserApps, 'riskRating', PIE_SIZE, variableScale, k => riskRatingDisplayNames[k])
            }
        };

    };

    $scope.$watch(
        () => viewOptions.options,
        refresh,
        true);

    vm.entityRef = { kind: 'ORG_UNIT', id };

    vm.eventDispatcher = new EventDispatcher();

    viewDataService
        .loadAll(id)
        .then(data => vm.rawViewData = data)
        .then(refresh);

    vm.onAssetBucketSelect = (bucket) => {
        $scope.$applyAsync(() => viewDataService.selectAssetBucket(bucket));
    };

    vm.loadFlowDetail = () => viewDataService.loadFlowDetail();
}

controller.$inject = [
    '$stateParams',
    '$scope',
    'OrgUnitViewDataService',
    'OrgUnitViewOptionsService',
    'HistoryStore'
];


export default {
    template: require('./unit-view.html'),
    controller,
    controllerAs: 'ctrl'
};

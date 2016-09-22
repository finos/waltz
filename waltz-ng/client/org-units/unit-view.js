
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


function controller($stateParams,
                    $scope,
                    viewDataService,
                    viewOptions,
                    historyStore,
                    tourService) {

    const id = $stateParams.id;
    const vm = this;

    const refresh = () => {
        if (!vm.rawViewData) return;
        const orgUnit = vm.rawViewData.orgUnit;

        historyStore.put(orgUnit.name, 'ORG_UNIT', 'main.org-unit.view', { id: orgUnit.id });

        vm.viewData = viewOptions.filter(vm.rawViewData);
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
        .then(refresh)
        .then(() => tourService.initialiseForKey('main.org-unit.view', true))
        .then(tour => vm.tour = tour);

    vm.onAssetBucketSelect = (bucket) => {
        $scope.$applyAsync(() => viewDataService.selectAssetBucket(bucket));
    };

    vm.loadFlowDetail = () => viewDataService
        .loadFlowDetail()
        .then(flowData => vm.viewData.dataFlows = flowData);
}


controller.$inject = [
    '$stateParams',
    '$scope',
    'OrgUnitViewDataService',
    'OrgUnitViewOptionsService',
    'HistoryStore',
    'TourService'
];


export default {
    template: require('./unit-view.html'),
    controller,
    controllerAs: 'ctrl'
};

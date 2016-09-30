
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


const addToHistory = (historyStore, orgUnit) => {
    historyStore.put(
        orgUnit.name,
        'ORG_UNIT',
        'main.org-unit.view',
        { id: orgUnit.id });
};


function controller($stateParams,
                    $scope,
                    viewDataService,
                    historyStore,
                    tourService) {

    const id = $stateParams.id;
    const vm = this;

    vm.entityRef = { kind: 'ORG_UNIT', id };

    vm.eventDispatcher = new EventDispatcher();

    vm.viewData = viewDataService.data;

    viewDataService
        .loadAll(id)
        .then(() => addToHistory(historyStore, vm.viewData.orgUnit))
        .then(() => tourService.initialiseForKey('main.org-unit.view', true))
        .then(tour => vm.tour = tour);

    vm.onAssetBucketSelect = (bucket) => {
        $scope.$applyAsync(() => viewDataService.selectAssetBucket(bucket));
    };

    vm.loadFlowDetail = () => viewDataService
        .loadFlowDetail()
        .then(flowData => vm.viewData.dataFlows = flowData);


    vm.loadOrgUnitDescendants = (id) => viewDataService
        .loadOrgUnitDescendants(id)
        .then(descendants => vm.viewData.orgUnitDescendants = descendants);

}


controller.$inject = [
    '$stateParams',
    '$scope',
    'OrgUnitViewDataService',
    'HistoryStore',
    'TourService'
];


export default {
    template: require('./unit-view.html'),
    controller,
    controllerAs: 'ctrl'
};

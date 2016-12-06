
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

const addToHistory = (historyStore, orgUnit) => {
    if (! orgUnit) { return; }
    historyStore.put(
        orgUnit.name,
        'ORG_UNIT',
        'main.org-unit.view',
        { id: orgUnit.id });
};


function initTour(tourService, holder = {}) {
    return tourService.initialiseForKey('main.org-unit.view', true)
        .then(tour => holder.tour = tour);
}

function processCosts(costs = []) {
    return _.chain(costs)
        .reduce((acc, x) => {
            const bucket = acc[x.application.id] || {total: 0, entityRef: x.application, costs: {}};
            bucket.costs[x.cost.kind] = x.cost.amount;
            bucket.total += x.cost.amount;

            acc[x.application.id] = bucket;
            return acc;
        }, {})
        .values()
        .orderBy('total', 'desc')
        .value();
}

function controller($stateParams,
                    $scope,
                    viewDataService,
                    assetCostStore,
                    historyStore,
                    tourService) {

    const id = $stateParams.id;
    const vm = this;

    vm.entityRef = { kind: 'ORG_UNIT', id };
    vm.viewData = viewDataService.data;

    viewDataService
        .loadAll(id)
        .then(() => addToHistory(historyStore, vm.viewData.orgUnit))
        .then(() => initTour(tourService, vm));


    // -- INTERACTIONS ---

    vm.onAssetBucketSelect = (bucket) => {
        $scope.$applyAsync(() => viewDataService.selectAssetBucket(bucket));
    };

    vm.loadFlowDetail = () => viewDataService
        .loadFlowDetail()
        .then(flowData => vm.viewData.dataFlows = flowData);

    vm.loadOrgUnitDescendants = (id) => viewDataService
        .loadOrgUnitDescendants(id)
        .then(descendants => vm.viewData.orgUnitDescendants = descendants);

    vm.lineageTableInitialised = (api) => {
        vm.exportLineageReports = api.export;
    };

    assetCostStore
        .findTopAppCostsByAppIdSelector({
            entityReference: vm.entityRef,
            scope: 'CHILDREN'
        })
        .then(costs => processCosts(costs))
        .then(costs => vm.viewData.topAssetCosts = costs);
}


controller.$inject = [
    '$stateParams',
    '$scope',
    'OrgUnitViewDataService',
    'AssetCostStore',
    'HistoryStore',
    'TourService'
];


export default {
    template: require('./unit-view.html'),
    controller,
    controllerAs: 'ctrl'
};

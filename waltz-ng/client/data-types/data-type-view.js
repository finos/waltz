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

import {initialiseData} from "../common";

const template = require('./data-type-view.html');


const initialState = {
    dataFlow: null,
    entityRef: null
};


function controller($scope,
                    dataType,
                    viewDataService,
                    dataFlowService,
                    historyStore) {

    const vm = initialiseData(this, initialState);

    const entityReference = {
        kind: 'DATA_TYPE',
        id: dataType.id
    };


    const selector = {
        entityReference,
        scope: 'CHILDREN'
    };

    vm.entityRef = entityReference;
    vm.dataType = dataType;

    vm.loadFlowDetail = () => viewDataService.loadFlowDetail();
    vm.onAssetBucketSelect = (bucket) => {
        $scope.$applyAsync(() => viewDataService.selectAssetBucket(bucket));
    };

    const refresh = () => {
        if (!vm.rawViewData) return;
        const dataType = vm.rawViewData.dataType;
        historyStore.put(dataType.name, 'DATA_TYPE', 'main.data-type.view', { id: dataType.id });
        vm.viewData = vm.rawViewData;
    };

    viewDataService
        .loadAll(dataType.id)
        .then(data => vm.rawViewData = data)
        .then(d => refresh());

    dataFlowService.initialise(selector)
        .then(() => dataFlowService.loadDetail())
        .then(flowData => vm.flowData = flowData)
        .then(d => console.log(d));
}


controller.$inject = [
    '$scope',
    'dataType',
    'DataTypeViewDataService',
    'DataFlowViewService',
    'HistoryStore'
];


export default {
    template,
    controller,
    controllerAs: 'ctrl'
};
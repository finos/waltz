/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016  Khartec Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import {initialiseData} from "../common";

import template from './data-type-view.html';


const initialState = {
    dataFlow: null,
    entityRef: null,
};


function controller($scope,
                    dataType,
                    viewDataService,
                    historyStore,
                    logicalDataFlowService,
                    tourService) {

    const vm = initialiseData(this, initialState);

    const entityReference = {
        kind: 'DATA_TYPE',
        id: dataType.id,
        name: dataType.name
    };


    const selector = {
        entityReference,
        scope: 'CHILDREN'
    };

    vm.entityRef = entityReference;
    vm.dataType = dataType;

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
        .then(d => refresh())
        .then(() => tourService.initialiseForKey('main.data-type.view', true))
        .then(tour => vm.tour = tour);


    logicalDataFlowService
        .initialise(selector)
        .then(flowData => vm.flowData = flowData)
        .then(() => logicalDataFlowService.loadDetail())
        .then(flowData => vm.flowData = flowData);

}


controller.$inject = [
    '$scope',
    'dataType',
    'DataTypeViewDataService',
    'HistoryStore',
    'LogicalFlowViewService',
    'TourService'
];


export default {
    template,
    controller,
    controllerAs: 'ctrl'
};
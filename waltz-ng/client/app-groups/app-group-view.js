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

import _ from "lodash";
import {CORE_API} from "../common/services/core-api-utils";

import template from "./app-group-view.html";


const initialState = {
    applications: [],
    assetCostData: null,
    bookmarks: [],
    capabilities: [],
    changeInitiatives: [],
    complexity: [],
    dataFlows : null,
    flowOptions: null,
    groupDetail: null,
    initiallySelectedIds: [],
    sourceDataRatings: [],
    techStats: null,
    user: null,
    visibility: {
        applicationOverlay: false,
        bookmarkOverlay: false,
        capabilityRatingOverlay: false,
        changeInitiativeOverlay: false,
        costOverlay: false,
        flowOverlay: false,
        techOverlay: false
    }
};


function controller($scope,
                    $q,
                    $stateParams,
                    serviceBroker,
                    appGroupStore,
                    assetCostViewService,
                    bookmarkStore,
                    complexityStore,
                    historyStore,
                    logicalFlowViewService,
                    measurableStore,
                    measurableCategoryStore,
                    measurableRatingStore,
                    sourceDataRatingStore,
                    userService) {

    const { id }  = $stateParams;

    const vm = Object.assign(this, initialState);

    const idSelector = {
        entityReference: {
            kind: 'APP_GROUP',
            id: id
        },
        scope: 'EXACT'
    };

    vm.entityRef = idSelector.entityReference;


    // -- LOAD ---

    measurableStore
        .findMeasurablesBySelector(idSelector)
        .then(measurables => vm.measurables = measurables);

    measurableCategoryStore
        .findAll()
        .then(cs => vm.measurableCategories = cs);

    measurableRatingStore
        .statsByAppSelector(idSelector)
        .then(ratings => vm.measurableRatings = ratings);

    logicalFlowViewService
        .initialise(id, 'APP_GROUP', 'EXACT')
        .then(flows => vm.dataFlows = flows);

    assetCostViewService
        .initialise(idSelector, 2016)
        .then(costs => vm.assetCostData = costs);

    bookmarkStore
        .findByParent({ id , kind: 'APP_GROUP'})
        .then(bookmarks => vm.bookmarks = bookmarks);

    appGroupStore.getById(id)
        .then(groupDetail => vm.groupDetail = groupDetail)
        .then(groupDetail => {
            historyStore.put(groupDetail.appGroup.name, 'APP_GROUP', 'main.app-group.view', { id });
            return groupDetail;
        })
        .then(groupDetail => _.map(groupDetail.applications, 'id'))
        .then(appIds => $q.all([
            serviceBroker.loadViewData(CORE_API.ApplicationStore.findBySelector, [ idSelector ]),
            complexityStore.findBySelector(id, 'APP_GROUP', 'EXACT'),
        ]))
        .then(([
            appsResponse,
            complexity
        ]) => {
            vm.applications = _.map(appsResponse.data, a => _.assign(a, {management: 'IT'}));
            vm.complexity = complexity;
        })
        .then(result => Object.assign(vm, result))
        .then(() => sourceDataRatingStore.findAll())
        .then((sourceDataRatings) => vm.sourceDataRatings = sourceDataRatings);

    userService
        .whoami()
        .then(u => vm.user = u);


    // -- INTERACT ---

    vm.isGroupEditable = () => {
        if (!vm.groupDetail) return false;
        if (!vm.user) return false;
        return _.some(vm.groupDetail.members, isUserAnOwner );
    };

    vm.loadAllCosts = () => {
        $scope.$applyAsync(() => {
            assetCostViewService.loadDetail()
                .then(data => vm.assetCostData = data);
        });
    };

    vm.loadFlowDetail = () => logicalFlowViewService
        .loadDetail()
        .then(flowData => vm.dataFlows = flowData);

    vm.loadRatingsDetail = () => {
        return vm.measurableRatingsDetail
            ? $q.resolve(vm.measurableRatingsDetail)
            : measurableRatingStore
                .findByAppSelector(idSelector)
                .then(rs => vm.measurableRatingsDetail = rs);
    };


    // -- HELPER ---

    const isUserAnOwner = member =>
        member.role === 'OWNER'
        && member.userId === vm.user.userName;
}


controller.$inject = [
    '$scope',
    '$q',
    '$stateParams',
    'ServiceBroker',
    'AppGroupStore',
    'AssetCostViewService',
    'BookmarkStore',
    'ComplexityStore',
    'HistoryStore',
    'LogicalFlowViewService',
    'MeasurableStore',
    'MeasurableCategoryStore',
    'MeasurableRatingStore',
    'SourceDataRatingStore',
    'UserService'
];


export default {
    template,
    controller,
    controllerAs: 'ctrl'
};

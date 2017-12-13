/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017 Waltz open source project
 * See README.md for more information
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
    capabilities: [],
    changeInitiatives: [],
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
        techOverlay: false
    }
};


function controller($q,
                    $stateParams,
                    serviceBroker,
                    appGroupStore,
                    historyStore,
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
    vm.selector =  idSelector;

    // -- LOAD ---

    appGroupStore.getById(id)
        .then(groupDetail => vm.groupDetail = groupDetail)
        .then(groupDetail => {
            vm.entityRef = Object.assign({}, vm.entityRef, {name: groupDetail.appGroup.name});
            historyStore.put(groupDetail.appGroup.name, 'APP_GROUP', 'main.app-group.view', { id });
            return groupDetail;
        })
        .then(groupDetail => _.map(groupDetail.applications, 'id'))
        .then(appIds => $q.all([
            serviceBroker.loadViewData(CORE_API.ApplicationStore.findBySelector, [ idSelector ]),
            serviceBroker.loadViewData(CORE_API.TechnologyStatisticsService.findBySelector, [ idSelector ])
        ]))
        .then(([
            appsResponse,
            techStatsResponse
        ]) => {
            vm.applications = _.map(appsResponse.data, a => _.assign(a, {management: 'IT'}));
            vm.techStats = techStatsResponse.data;
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

    // -- HELPER ---

    const isUserAnOwner = member =>
        member.role === 'OWNER'
        && member.userId === vm.user.userName;
}


controller.$inject = [
    '$q',
    '$stateParams',
    'ServiceBroker',
    'AppGroupStore',
    'HistoryStore',
    'SourceDataRatingStore',
    'UserService'
];


export default {
    template,
    controller,
    controllerAs: 'ctrl'
};

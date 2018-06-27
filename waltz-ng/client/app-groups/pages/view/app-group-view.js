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

import {CORE_API} from "../../../common/services/core-api-utils";

import template from "./app-group-view.html";


const initialState = {
    groupDetail: null,
    sections: [],
    availableSections: []
};


function controller($stateParams,
                    dynamicSectionManager,
                    serviceBroker,
                    historyStore) {

    const { id }  = $stateParams;

    const vm = Object.assign(this, initialState);

    vm.entityRef = {
        id,
        kind: 'APP_GROUP'
    };

    // -- BOOT --
    vm.$onInit = () => {
        vm.availableSections = dynamicSectionManager.findAvailableSectionsForKind('APP_GROUP');
        vm.sections = dynamicSectionManager.findUserSectionsForKind('APP_GROUP');

        serviceBroker
            .loadViewData(CORE_API.AppGroupStore.getById, [id])
            .then(r => {
                vm.groupDetail = r.data;
                vm.entityRef = Object.assign({}, vm.entityRef, {name: vm.groupDetail.appGroup.name});
                historyStore.put(
                    vm.groupDetail.appGroup.name,
                    'APP_GROUP',
                    'main.app-group.view',
                    { id });
            });

    };

    // -- INTERACT --
    vm.addSection = (section) => vm.sections = dynamicSectionManager.openSection(section, 'APP_GROUP');
    vm.removeSection = (section) => vm.sections = dynamicSectionManager.removeSection(section, 'APP_GROUP');


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
    '$stateParams',
    'DynamicSectionManager',
    'ServiceBroker',
    'HistoryStore'
];


export default {
    template,
    controller,
    controllerAs: 'ctrl'
};

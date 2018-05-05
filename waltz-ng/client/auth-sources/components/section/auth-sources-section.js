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

import template from './auth-sources-section.html';
import {initialiseData} from "../../../common/index";
import {CORE_API} from "../../../common/services/core-api-utils";

const bindings = {
    parentEntityRef: '<',
    showNonAuthSources: '@?'
};


const initialState = {
    authSources: [],
    showNonAuthSources: true,
    visibility: {
        tab: 0,
        sourceDataRatingsOverlay: false,
        authSourcesList: false
    }
};


function controller(serviceBroker) {
    const vm = initialiseData(this, initialState);

    const loadNonAuthSources = () => {
        serviceBroker
            .loadViewData(
                CORE_API.AuthSourcesStore.findNonAuthSources,
                [vm.parentEntityRef])
            .then(r => vm.nonAuthSources = r.data);
    };

    const loadAuthSources = () => {
        serviceBroker
            .loadViewData(
                CORE_API.AuthSourcesStore.findAuthSources,
                [vm.parentEntityRef])
            .then(r => {
                vm.authSources = r.data;
            })
    };

    vm.$onInit = () => {
        vm.visibility.tab = vm.parentEntityRef.kind === 'DATA_TYPE'
            ? 1
            : 0;

        vm.visibility.authSourcesList = vm.parentEntityRef.kind === 'ORG_UNIT' || vm.parentEntityRef.kind === 'DATA_TYPE'
    };

    vm.tabSelected = (name, idx) => {
        vm.visibility.tab = idx;
        vm.visibility.editBtn = false;
        switch(name) {
            case 'summary':
                break;
            case 'authSources':
                loadAuthSources();
                vm.visibility.editBtn = vm.parentEntityRef.kind === 'ORG_UNIT';
                break;
            case 'nonAuthSources':
                loadNonAuthSources();
                break;
        }
    };

    vm.toggleSourceDataRatingOverlay = () =>
        vm.visibility.sourceDataRatingsOverlay = !vm.visibility.sourceDataRatingsOverlay;

}


controller.$inject = ['ServiceBroker'];


export const component = {
    bindings,
    controller,
    template
};


export const id = 'waltzAuthSourcesSection';
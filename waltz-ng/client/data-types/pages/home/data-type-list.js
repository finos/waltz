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

import angular from "angular";

import {initialiseData} from "../../../common";
import {CORE_API} from "../../../common/services/core-api-utils";

import template from './data-type-list.html';


const initialState = {
    visibility: {
        editor: false
    }
};


function controller($state,
                    serviceBroker) {

    const vm = initialiseData(this, initialState);

    const loadAuthSources = ()  => {
        serviceBroker
            .loadViewData(CORE_API.AuthSourcesStore.findAll)
            .then(r => vm.authSources = r.data);
    };

    vm.$onInit = () => {
        serviceBroker
            .loadAppData(CORE_API.SvgDiagramStore.findByGroup, ['DATA_TYPE'])
            .then(r => vm.diagrams = r.data);

        loadAuthSources();

    };

    vm.nodeSelected = (node) => vm.selectedNode = node;

    vm.blockProcessor = b => {
        b.block.onclick = () => $state.go('main.data-type.code', { code: b.value });
        angular.element(b.block).addClass('clickable');
    };

    vm.showAuthSources = () => {
        vm.visibility.editor = false;
        loadAuthSources();
    };

    vm.editAuthSources = () => {
        vm.visibility.editor = true;
    };
}


controller.$inject = [
    '$state',
    'ServiceBroker'
];


const view = {
    template,
    controllerAs: 'ctrl',
    controller
};


export default view;
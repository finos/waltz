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

import {initialiseData} from "../../common";
import {CORE_API} from "../../common/services/core-api-utils";
import template from './change-log-section.html';


const bindings = {
    parentEntityRef: '<',
    userName: '<'
};


const initialState = {
    entries: []
};




function controller(serviceBroker) {
    const vm = initialiseData(this, initialState);

    const loadEntries = () => {
        const dataLoadHandler = (result) => {
            vm.entries = result.data;
        };

        if (vm.parentEntityRef) {
            serviceBroker
                .loadViewData(
                    CORE_API.ChangeLogStore.findByEntityReference,
                    [vm.parentEntityRef],
                    { force: true })
                .then(dataLoadHandler);
        } else if (vm.userName) {
            serviceBroker
                .loadViewData(
                    CORE_API.ChangeLogStore.findForUserName,
                    [vm.userName],
                    { force: true })
                .then(dataLoadHandler);
        }
    };

    vm.$onChanges = (changes) => {
        if (vm.parentEntityRef || vm.userName) {
            loadEntries();
        }
    };

    vm.refresh = () => loadEntries();

    vm.changeLogTableInitialised = (api) => {
        vm.exportChangeLog = () => api.exportFn("change-log.csv");
    };
}


controller.$inject = [
    'ServiceBroker'
];


const component = {
    bindings,
    template,
    controller
};


export default component;

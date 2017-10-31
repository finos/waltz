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
import {initialiseData} from "../common";
import {CORE_API} from "../common/services/core-api-utils";


const initialState = {
    entries: [],
    entityRef: null
};


function controller($stateParams,
                    serviceBroker) {

    const vm = initialiseData(this, initialState);

    const entityRef = {
        kind: $stateParams.kind,
        id: _.toNumber($stateParams.id),
        name: $stateParams.name
    };

    vm.changeLogTableInitialised = (api) => {
        vm.exportChangeLog = () => api.exportFn("change-log.csv");
    };

    vm.entityRef = entityRef;

    serviceBroker
        .loadViewData(CORE_API.ChangeLogStore.findByEntityReference, [vm.entityRef, null])
        .then(result => vm.entries = result.data);
}


controller.$inject = [
    '$stateParams',
    'ServiceBroker'
];


const view = {
    template: require('./view.html'),
    controller,
    controllerAs: 'ctrl'
};


export default view;

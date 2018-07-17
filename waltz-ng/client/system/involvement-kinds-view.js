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
import {initialiseData} from "../common";
import template from './involvement-kinds-view.html';

const initialState = {
    involvementKinds: [],
    creatinginvolvementKind: false,
    newinvolvementKind: {}
};


function controller($q,
                    involvementKindService,
                    notification) {

    const vm = initialiseData(this, initialState);

    function update(id, change) {
        const updateCmd = Object.assign(change, { id });
        return involvementKindService.update(updateCmd)
            .then(() => notification.success('Updated'));
    }

    vm.updateName = (id, change) => {
        if(change.newVal === "") return $q.reject("Too short");
        return update(id, { name: change })
            .then(() => _.find(vm.involvementKinds, {'id': id}).name = change.newVal);
    };

    vm.updateDescription = (id, change) => {
        if(change.newVal === "") return $q.reject("Too short");
        return update(id, { description: change })
            .then(() => _.find(vm.involvementKinds, {'id': id}).description = change.newVal);
    };


    vm.startNewinvolvementKind = () => {
        vm.creatinginvolvementKind = true;
    };

    vm.saveNewinvolvementKind = () => {
        involvementKindService
            .create(vm.newinvolvementKind)
            .then(id => {
                notification.success('Created');
                vm.creatinginvolvementKind = false;
                vm.newinvolvementKind = {};
                loadInvolvementKinds();
            });


    };

    vm.cancelNewinvolvementKind = () => {
        vm.creatinginvolvementKind = false;
    };


    function loadInvolvementKinds() {
        involvementKindService
            .loadInvolvementKinds()
            .then(kinds => {
                vm.involvementKinds = kinds;
            });
    }

    loadInvolvementKinds();
}


controller.$inject = [
    '$q',
    'InvolvementKindService',
    'Notification'
];


export default {
    template,
    controller,
    controllerAs: 'ctrl',
    bindToController: true,
    scope: {}
};

/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017, 2018, 2019 Waltz open source project
 * See README.md for more information
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific
 *
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

    vm.updateName = (change, kind) => {
        if(change.newVal === "") return $q.reject("Too short");
        return update(kind.id, { name: change })
            .then(() => _.find(vm.involvementKinds, {'id': kind.id}).name = change.newVal);
    };

    vm.updateDescription = (change, kind) => {
        if(change.newVal === "") return $q.reject("Too short");
        return update(kind.id, { description: change })
            .then(() => _.find(vm.involvementKinds, {'id': kind.id}).description = change.newVal);
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

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
import template from "./involvement-kinds-view.html";
import toasts from "../svelte-stores/toast-store";
import {displayError} from "../common/error-utils";

const initialState = {
    involvementKinds: [],
    creatingInvolvementKind: false,
    newInvolvementKind: {}
};


function controller($q,
                    involvementKindService) {

    const vm = initialiseData(this, initialState);

    function update(id, change) {
        const updateCmd = Object.assign(change, { id });
        return involvementKindService.update(updateCmd)
            .then(() => toasts.success("Updated"))
            .catch(e => displayError(
                `Failed to apply change: ${JSON.stringify(change)}`,
                e));
    }

    vm.updateName = (change, kind) => {
        if(change.newVal === "") return $q.reject("Too short");
        return update(kind.id, {name: change})
            .then(() => _.find(vm.involvementKinds, {"id": kind.id}).name = change.newVal);
    };

    vm.updateDescription = (change, kind) => {
        if (change.newVal === "") return $q.reject("Too short");
        return update(kind.id, {description: change})
            .then(() => _.find(vm.involvementKinds, {"id": kind.id}).description = change.newVal);
    };

    vm.updateExternalId = (change, kind) => {
        if (change.newVal === "") return $q.reject("Too short");
        return update(kind.id, {externalId: change})
            .then(() => _.find(vm.involvementKinds, {"id": kind.id}).externalId = change.newVal);
    };


    vm.startNewInvolvementKind = () => {
        vm.creatingInvolvementKind = true;
    };

    vm.saveNewInvolvementKind = () => {
        involvementKindService
            .create(vm.newInvolvementKind)
            .then(id => {
                toasts.success("Created");
                vm.creatingInvolvementKind = false;
                vm.newInvolvementKind = {};
                loadInvolvementKinds();
            })
            .catch(e => displayError(
                "Failed to create involvement kind",
                e));
    };

    vm.cancelNewInvolvementKind = () => {
        vm.creatingInvolvementKind = false;
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
    "$q",
    "InvolvementKindService"
];


export default {
    template,
    controller,
    controllerAs: "ctrl",
    bindToController: true,
    scope: {}
};

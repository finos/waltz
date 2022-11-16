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
import template from "./actors-view.html";
import {displayError} from "../common/error-utils";
import toasts from "../svelte-stores/toast-store";


const initialState = {
    actors: [],
    creatingActor: false,
    newActor: { isExternal: false }
};


function controller($q,
                    actorService) {

    const vm = initialiseData(this, initialState);

    function update(actor, change) {
        const updateCmd = Object.assign(change, { id: actor.id });
        return actorService.update(updateCmd)
            .then(() => toasts.success("Updated"))
            .catch(e => displayError("Could not add update actor", e));
    }

    function loadActors() {
        actorService
            .loadActors()
            .then(kinds => vm.actors = kinds);
    }

    vm.updateName = (change, actor) => {
        if(change.newVal === "") return $q.reject("Too short");
        return update(actor, { name: change })
            .then(() => _.find(vm.actors, {"id": actor.id}).name = change.newVal);

    };

    vm.updateDescription = (change, actor) => {
        if(change.newVal === "") return $q.reject("Too short");
        return update(actor, { description: change })
            .then(() => _.find(vm.actors, {"id": actor.id}).description = change.newVal);
    };

    vm.updateIsExternal = (change, actor) => {
        if(change.newVal === null) return $q.reject("No value provided");
        return update(actor, { isExternal: change })
            .then(() => _.find(vm.actors, {"id": actor.id}).isExternal = change.newVal);
    };

    vm.updateExternalId = (change, actor) => {
        if(change.newVal === null) return $q.reject("No value provided");
        return update(actor, { externalId: change })
            .then(() => _.find(vm.actors, {"id": actor.id}).externalId = change.newVal);
    };

    vm.startNewActor = () => {
        vm.creatingActor = true;
    };

    vm.saveNewActor = () => {
        actorService
            .create(vm.newActor)
            .then(() => {
                toasts.success("Created");
                vm.creatingActor = false;
                vm.newActor = {};
                loadActors();
            })
            .catch(e => displayError("Could not create actor", e));
    };

    vm.cancelNewActor = () => {
        vm.creatingActor = false;
    };

    vm.selectActor = (actor) => {
        vm.selectedActor = actor;
    };

    // --- boot --

    loadActors();
}


controller.$inject = [
    "$q",
    "ActorService"
];


export default {
    template,
    controller,
    controllerAs: "$ctrl",
    bindToController: true,
    scope: {}
};

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

const initialState = {
    actors: [],
    creatingActor: false,
    newActor: { isExternal: false }
};


function controller($q,
                    actorService,
                    notification) {

    const vm = initialiseData(this, initialState);

    function update(actor, change) {
        const updateCmd = Object.assign(change, { id: actor.id });
        return actorService.update(updateCmd)
            .then(() => notification.success("Updated"));
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


    vm.startNewActor = () => {
        vm.creatingActor = true;
    };

    vm.saveNewActor = () => {
        actorService
            .create(vm.newActor)
            .then(() => {
                notification.success("Created");
                vm.creatingActor = false;
                vm.newActor = {};
                loadActors();
            });


    };

    vm.cancelNewActor = () => {
        vm.creatingActor = false;
        console.log("cancelled new");
    };


    function loadActors() {
        actorService
            .loadActors()
            .then(kinds => {
                vm.actors = kinds;
            });
    }

    loadActors();

    vm.selectActor = (actor) => {
        vm.selectedActor = actor;
    }


}


controller.$inject = [
    "$q",
    "ActorService",
    "Notification",
];


export default {
    template,
    controller,
    controllerAs: "$ctrl",
    bindToController: true,
    scope: {}
};

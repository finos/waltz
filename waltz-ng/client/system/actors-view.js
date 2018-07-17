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
import template from './actors-view.html';

const initialState = {
    actors: [],
    creatingActor: false,
    newActor: { isExternal: false }
};


function controller($q,
                    actorService,
                    notification) {

    const vm = initialiseData(this, initialState);

    function update(id, change) {
        const updateCmd = Object.assign(change, { id });
        return actorService.update(updateCmd)
            .then(() => notification.success('Updated'));
    }

    vm.updateName = (id, change) => {
        if(change.newVal === "") return $q.reject("Too short");
        return update(id, { name: change })
            .then(() => _.find(vm.actors, {'id': id}).name = change.newVal);
    };

    vm.updateDescription = (id, change) => {
        if(change.newVal === "") return $q.reject("Too short");
        return update(id, { description: change })
            .then(() => _.find(vm.actors, {'id': id}).description = change.newVal);
    };

    vm.updateIsExternal = (id, change) => {
        if(change.newVal === null) return $q.reject("No value provided");
        return update(id, { isExternal: change })
            .then(() => _.find(vm.actors, {'id': id}).isExternal = change.newVal);
    };


    vm.startNewActor = () => {
        vm.creatingActor = true;
    };

    vm.saveNewActor = () => {
        actorService
            .create(vm.newActor)
            .then(() => {
                notification.success('Created');
                vm.creatingActor = false;
                vm.newActor = {};
                loadActors();
            });


    };

    vm.cancelNewActor = () => {
        vm.creatingActor = false;
        console.log('cancelled new');
    };


    function loadActors() {
        actorService
            .loadActors()
            .then(kinds => {
                vm.actors = kinds;
            });
    }

    loadActors();
}


controller.$inject = [
    '$q',
    'ActorService',
    'Notification'
];


export default {
    template,
    controller,
    controllerAs: 'ctrl',
    bindToController: true,
    scope: {}
};

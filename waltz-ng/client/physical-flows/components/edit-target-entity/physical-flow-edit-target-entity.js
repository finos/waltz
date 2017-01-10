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

import {initialiseData, invokeFunction} from "../../../common";


const bindings = {
    current: '<',
    existingTargets: '<',  // [ <entityRef>... ]
    owningEntity: '<',
    onDismiss: '<',
    onChange: '<'
};


const template = require('./physical-flow-edit-target-entity.html');


const initialState = {
    searchEntityKind: null,
    existingTargets: []
};


function sameApp(app, owningEntity) {
    return owningEntity.kind !== 'ACTOR' && app.id === owningEntity.id;
}


function sameActor(actor, owningEntity) {
    return owningEntity.kind === 'ACTOR' && actor.id === owningEntity.id;
}


function controller(actorStore) {
    const vm = initialiseData(this, initialState);

    actorStore
        .findAll()
        .then((actors) => vm.allActors = actors );

    vm.$onChanges = (changes) => {
        if(vm.current && vm.current.kind) {
            vm.searchEntityKind = vm.current.kind === 'ACTOR' ? vm.current.kind : 'APPLICATION';
        }
    };

    vm.cancel = () => {
        vm.actorDuplicate = false;
        vm.appDuplicate = false;
        vm.searchEntityKind = null;
        invokeFunction(vm.onDismiss);
    };

    vm.selectApp = (app) => {
        vm.appDuplicate = sameApp(app, vm.owningEntity);
    };

    vm.addActor = (actor) => {
        vm.actorDuplicate = sameActor(actor, vm.owningEntity);
        if(!vm.actorDuplicate) {
            invokeFunction(vm.onChange, actor);
        }
    };

    vm.addApp = (app) => {
        const appWithKind = Object.assign({}, app, {kind: 'APPLICATION'});
        vm.appDuplicate = sameApp(appWithKind, vm.owningEntity);
        if(!vm.appDuplicate) {
            invokeFunction(vm.onChange, appWithKind);
        }
    };


    vm.selectActor = (actor) => {
        vm.actorDuplicate = sameActor(actor, vm.owningEntity);
    };

}


controller.$inject = [
    'ActorStore'
];


const component = {
    bindings,
    template,
    controller
};


export default component;


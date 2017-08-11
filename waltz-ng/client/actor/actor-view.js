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

import _ from 'lodash';
import {initialiseData} from "../common";

import template from "./actor-view.html";


const initialState = {
    logs: [],
};


function mkHistoryObj(actor) {
    return {
        name: actor.name,
        kind: 'ACTOR',
        state: 'main.actor.view',
        stateParams: { id: actor.id }
    };
}


function addToHistory(historyStore, actor) {
    if (! actor) { return; }

    const historyObj = mkHistoryObj(actor);

    historyStore.put(
        historyObj.name,
        historyObj.kind,
        historyObj.state,
        historyObj.stateParams);
}



function controller($stateParams,
                    actorStore,
                    historyStore) {

    const vm = initialiseData(this, initialState);

    const id = $stateParams.id;
    vm.entityRef = { kind: 'ACTOR', id };
    Object.assign(vm, { id, entityRef: vm.entityRef });

    actorStore
        .getById(id)
        .then(a => vm.actor = a)
        .then(() => vm.entityRef = Object.assign({}, vm.entityRef, { name: vm.actor.name }))
        .then(() => addToHistory(historyStore, vm.actor));


    vm.createFlowDiagramCommands = () => {
        const actor = Object.assign({}, vm.actor, { kind: 'ACTOR' });
        const title = `${actor.name} flows`;
        const annotation = {
            id: +new Date()+'',
            kind: 'ANNOTATION',
            entityReference: actor,
            note: `${actor.name} data flows`
        };

        const modelCommands = [
            { command: 'ADD_NODE', payload: actor },
            { command: 'ADD_ANNOTATION', payload: annotation },
            { command: 'SET_TITLE', payload: title }
        ];

        const moveCommands = [
            { command: 'MOVE', payload: { id: `ANNOTATION/${annotation.id}`, dx: 100, dy: -50 }},
            { command: 'MOVE', payload: { id: `ACTOR/${actor.id}`, dx: 300, dy: 200 }},
        ];

        return _.concat(modelCommands, moveCommands);
    };

}


controller.$inject = [
    '$stateParams',
    'ActorStore',
    'HistoryStore',
];


const view = {
    template,
    controller,
    controllerAs: 'ctrl'
};

export default view;

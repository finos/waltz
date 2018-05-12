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

import _ from 'lodash';
import {initialiseData} from '../../../common';
import {toGraphId} from '../../flow-diagram-utils';

import template from './flow-diagram-node-popup.html';


const bindings = {
    onDismiss: '<',
    existingEntities: '<',
    commandProcessor: '<'
};


const initialState = {
    existingEntities: [],
    selectedNode: null
};


function sameRef(r1, r2) {
    return r1.kind === r2.kind && r1.id === r2.id;
}


function controller() {
    const vm = this;

    vm.$onInit = () => initialiseData(vm, initialState);

    vm.onNodeSelected = (entityRef) => {
        vm.selectedNode = entityRef;
    };

    vm.nodeSelectionFilter = (entityRef) => {
        return !_.find(vm.existingEntities, e => sameRef(e, entityRef));
    };

    vm.send = () => {
        const addCmd = {
            command: 'ADD_NODE',
            payload: vm.selectedNode
        };
        const dx = _.random(-80, 80);
        const dy = _.random(50, 80);

        const moveCmd = {
            command: 'MOVE',
            payload: {
                id: toGraphId(vm.selectedNode),
                dx,
                dy
            }
        };

        vm.commandProcessor([addCmd, moveCmd]);
    };
}


controller.$inject = [];


const component = {
    template,
    bindings,
    controller
};


export default component;
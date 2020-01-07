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
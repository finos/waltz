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

import {initialiseData} from '../../../common';
import {toGraphId} from '../../flow-diagram-utils';
import template from './flow-diagram-annotation-popup.html';


/**
 * @name waltz-flow-diagram-annotation-popup
 *
 * @description
 * This component ...
 */


const bindings = {
    onDismiss: '<',
    annotation: '<',
    commandProcessor: '<'
};


const initialState = {};


function controller() {
    const vm = this;

    vm.$onInit = () => initialiseData(vm, initialState);

    vm.$onChanges = (c) => {
        vm.title = vm.annotation.id ? 'Update Annotation' : 'Add Annotation';
        vm.actionLabel = vm.annotation.id ? 'Update' : 'Add';
        vm.note = vm.annotation.note;
    };

    vm.send = () => {

        const commands = [];

        if (vm.annotation.id) {
            commands.push({
                command: 'UPDATE_ANNOTATION',
                payload: {
                    note: vm.note,
                    id: toGraphId(vm.annotation)
                }
            });
        } else {
            const payload =  Object.assign({}, vm.annotation, { note: vm.note, id: +new Date()+'', kind: 'ANNOTATION' });
            commands.push({
                command: 'ADD_ANNOTATION',
                payload
            });
            commands.push({
                command: 'MOVE',
                payload: {
                    id: toGraphId(payload),
                    dx: 30,
                    dy: 30
                }
            });

        }
        vm.commandProcessor(commands);

    };
}


controller.$inject = [];


const component = {
    template,
    bindings,
    controller
};


export default component;
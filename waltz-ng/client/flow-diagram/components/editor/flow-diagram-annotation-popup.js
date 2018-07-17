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
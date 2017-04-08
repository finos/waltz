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
import {initialiseData} from '../../../common';
import {toGraphId} from '../../flow-diagram-utils';

/**
 * @name waltz-flow-diagram-add-flow-popup
 *
 * @description
 * This component ...
 */


const bindings = {
    flows: '<',
    node: '<',
    isUpstream: '<',
    commandProcessor: '<',
    onDismiss: '<'
};


const initialState = {};


const template = require('./flow-diagram-add-flow-popup.html');



function convertFlowsToOptions(flows = [], node, isUpstream) {
    const counterpart = isUpstream
        ? 'source'
        : 'target';

    const self = isUpstream
        ? 'target'
        : 'source';

    return _
        .chain(flows)
        .filter(f => f[self].id === node.id)
        .map(f => Object.assign({}, f, { kind: 'LOGICAL_DATA_FLOW' }))
        .map(f => {
            const counterpartEntity = f[counterpart];
            const baseEntity = f[self];
            const dx = _.random(-80, 80);
            const dy = _.random(50, 80) * (isUpstream ? -1 : 1);
            return {
                entity: counterpartEntity,
                commands: [
                    { command: 'ADD_NODE', payload: counterpartEntity },
                    { command: 'ADD_FLOW', payload: f },
                    { command: 'MOVE', payload: { id: toGraphId(counterpartEntity), refId: toGraphId(baseEntity), dx, dy } }
                ]
            };
        })
        .value();
}


function controller() {
    const vm = this;

    vm.$onInit = () => initialiseData(vm, initialState);

    vm.$onChanges = (c) => {
        const description = vm.isUpstream
            ? 'Select an upstream node from the list below:'
            : 'Select a downstream node from the list belows:';
        const direction = vm.isUpstream
            ? 'Upstream'
            : 'Downstream';

        vm.description = description;
        vm.title = `Add ${direction} node for ${vm.node.name}`;
        vm.options = convertFlowsToOptions(vm.flows, vm.node, vm.isUpstream);
    };

}


controller.$inject = [];


const component = {
    template,
    bindings,
    controller
};


export default component;
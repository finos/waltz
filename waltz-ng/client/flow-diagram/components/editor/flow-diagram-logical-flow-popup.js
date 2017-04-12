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
 * @name waltz-flow-diagram-logical-flow-popup
 *
 * @description
 * This component ...
 */


const bindings = {
    logicalFlows: '<',
    existingEntities: '<',
    node: '<',
    isUpstream: '<',
    commandProcessor: '<',
    onDismiss: '<'
};


const initialState = {
    logicalFlows: [],
    existingEntities: [],
    node: null,
    isUpstream: true,
    commandProcessor: () => console.log('wdlfp: default command processor'),
    onDismiss: () => console.log('wdlfp: default on-dismiss'),
};


const template = require('./flow-diagram-logical-flow-popup.html');


function sameRef(r1, r2) {
    return r1.kind === r2.kind && r1.id === r2.id;
}


function convertFlowsToOptions(logicalFlows = [], node, isUpstream, existingEntities = []) {
    const counterpartPropName = isUpstream
        ? 'source'
        : 'target';

    const selfPropName = isUpstream
        ? 'target'
        : 'source';

    return _
        .chain(logicalFlows)
        .filter(f => f[selfPropName].id === node.id)
        .map(f => Object.assign({}, f, { kind: 'LOGICAL_DATA_FLOW' }))
        .map(f => {
            const counterpartEntity = f[counterpartPropName];
            const baseEntity = f[selfPropName];

            if (sameRef(counterpartEntity, baseEntity)) return null;

            const flowExists = _.some(existingEntities, ref => sameRef(ref, f));
            const counterpartExists = _.some(existingEntities, ref => sameRef(ref, counterpartEntity));


            const commands = [];

            if (!counterpartExists) {
                commands.push({
                    command: 'ADD_NODE',
                    payload: counterpartEntity });

                const dx = _.random(-80, 80);
                const dy = _.random(50, 80) * (isUpstream ? -1 : 1);

                commands.push({
                    command: 'MOVE',
                    payload: {
                        id: toGraphId(counterpartEntity),
                        refId: toGraphId(baseEntity),
                        dx,
                        dy
                    } });
            }

            if (! flowExists) {
                commands.push({
                    command: 'ADD_FLOW',
                    payload: f });
            }

            return {
                entity: counterpartEntity,
                disabled: flowExists,
                commands
            };
        })
        .filter(opt => opt != null)
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
        vm.options = convertFlowsToOptions(vm.logicalFlows, vm.node, vm.isUpstream, vm.existingEntities);
    };

}


controller.$inject = [];


const component = {
    template,
    bindings,
    controller
};


export default component;
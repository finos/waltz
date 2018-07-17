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
import {initialiseData} from "../../../common";
import {toGraphId} from "../../flow-diagram-utils";
import {sameRef} from "../../../common/entity-utils";
import {kindToViewState} from "../../../common/link-utils";
import template from './flow-diagram-logical-flow-popup.html';

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
    commandProcessor: () => console.log('wfdlfp: default command processor'),
    onDismiss: () => console.log('wfdlfp: default on-dismiss'),
};


function mkFlows(logicalFlows = [], node, isUpstream, existingEntities = []) {
    const counterpartPropName = isUpstream
        ? 'source'
        : 'target';

    const selfPropName = isUpstream
        ? 'target'
        : 'source';

    return _
        .chain(logicalFlows)
        .filter(f => f[selfPropName].id === node.id)
        .reject(f => f[counterpartPropName].id === node.id)
        .map(f => Object.assign({}, f, { kind: 'LOGICAL_DATA_FLOW' }))
        .map(f => {
            const counterpartEntity = f[counterpartPropName];
            const flowExists = _.some(existingEntities, ref => sameRef(ref, f));
            return {
                counterpartEntity,
                logicalFlow: f,
                used: flowExists,
                existing: flowExists
            };
        })
        .sortBy(d => d.counterpartEntity.name.toLowerCase())
        .value();
}


function prepareUpdateCommands(flows = [], existingEntities = [], isUpstream, baseEntity) {
    const additions = _.filter(flows, f => ! f.existing && f.used);
    const removals = _.filter(flows, f => f.existing && ! f.used);

    const nodeAdditionCommands = _
        .chain(additions)
        .reject(f => _.some(existingEntities, ent => sameRef(ent, f.counterpartEntity)))
        .flatMap(f => {
            const addCmd = {
                command: 'ADD_NODE',
                payload: f.counterpartEntity
            };
            const dx = _.random(-80, 80);
            const dy = _.random(50, 80) * (isUpstream ? -1 : 1);

            const moveCmd = {
                command: 'MOVE',
                payload: {
                    id: toGraphId(f.counterpartEntity),
                    refId: toGraphId(baseEntity),
                    dx,
                    dy
                }
            };
            return [addCmd, moveCmd];
        })
        .value();

    const flowAdditionCommands = _.map(additions, f => {
        return {
            command: 'ADD_FLOW',
            payload: f.logicalFlow
        };
    });

    const flowRemovalCommands = _.map(removals, f => {
        return {
            command: 'REMOVE_FLOW',
            payload: {
                id: toGraphId(f.logicalFlow),
                source: toGraphId(f.logicalFlow.source),
                target: toGraphId(f.logicalFlow.target)
            }
        };
    });


    return _.concat(nodeAdditionCommands, flowAdditionCommands, flowRemovalCommands);
}


function controller($state) {
    const vm = this;

    vm.$onInit = () => initialiseData(vm, initialState);

    vm.$onChanges = (c) => {
        const description = vm.isUpstream
            ? 'Select upstream nodes from the list below:'
            : 'Select downstream nodes from the list below:';
        const direction = vm.isUpstream
            ? 'Upstream'
            : 'Downstream';

        vm.description = description;
        vm.title = `Add ${direction} nodes for ${vm.node.name}`;
        vm.flows = mkFlows(vm.logicalFlows, vm.node, vm.isUpstream, vm.existingEntities);

        vm.logicalFlowAdditionUrl = $state.href(
            kindToViewState(vm.node.kind),
            { id: vm.node.id });
    };

    vm.update = () => {
        const commands = prepareUpdateCommands(vm.flows, vm.existingEntities, vm.isUpstream , vm.node);
        vm.commandProcessor(commands);
    };

}


controller.$inject = [
    '$state',
];


const component = {
    template,
    bindings,
    controller
};


export default component;
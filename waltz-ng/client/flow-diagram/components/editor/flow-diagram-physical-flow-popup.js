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
import template from './flow-diagram-physical-flow-popup.html';

/**
 * @name waltz-flow-diagram-physical-flow-popup
 *
 * @description
 * This component ...
 */


const bindings = {
    logicalFlow: '<',
    physicalFlows: '<',
    physicalSpecifications: '<',
    existingEntities: '<',
    commandProcessor: '<',
    onDismiss: '<'
};


const initialState = {
    logicalFlows: [],
    existingEntities: [],
    commandProcessor: () => console.log('wdpfp: default command processor'),
    onDismiss: () => console.log('wdpfp: default on-dismiss'),
};


function sameRef(r1, r2) {
    return r1.kind === r2.kind && r1.id === r2.id;
}


function prepareFlows(
    physicalFlows = [],
    physicalSpecifications = [],
    existingEntities = [])
{
    const specsById = _.keyBy(physicalSpecifications, 'id');
    return _.chain(physicalFlows)
        .map(f => {
            const currentlyUsed = _.some(existingEntities, existing => sameRef(existing, { kind: 'PHYSICAL_FLOW', id: f.id }))
            return {
                used: currentlyUsed,
                existing: currentlyUsed,
                physicalFlow: f,
                specification: specsById[f.specificationId]
            };
        })
        .sortBy(d => d.specification.name.toLowerCase())
        .value();
}


function controller($state) {
    const vm = this;

    vm.$onInit = () => initialiseData(vm, initialState);

    vm.$onChanges = (c) => {
        const description = "Define the physical flows";
        vm.description = description;
        vm.title = `Define Physical Flows`;
        vm.flows = prepareFlows(
            vm.physicalFlows,
            vm.physicalSpecifications,
            vm.existingEntities);

        vm.physicalFlowRegisterUrl = $state.href(
            'main.physical-flow.registration',
            {
                kind: vm.logicalFlow.source.kind,
                id: vm.logicalFlow.source.id,
                targetLogicalFlowId: vm.logicalFlow.id
            });

    };

    vm.update = () => {
        const additions = _.filter(vm.flows, f => ! f.existing && f.used);
        const removals = _.filter(vm.flows, f => f.existing && ! f.used);

        const additionCommands = _.map(additions, f => {
            return {
                command: 'ADD_DECORATION',
                payload: {
                    ref: {
                        id: vm.logicalFlow.id,
                        kind: vm.logicalFlow.kind
                    },
                    decoration: {
                        id: f.physicalFlow.id,
                        kind: 'PHYSICAL_FLOW'
                    }
                }
            };
        });

        const removalCommands = _.map(removals, f => {
            return {
                command: 'REMOVE_DECORATION',
                payload: {
                    ref: {
                        id: vm.logicalFlow.id,
                        kind: vm.logicalFlow.kind
                    },
                    decoration: {
                        id: f.physicalFlow.id,
                        kind: 'PHYSICAL_FLOW'
                    }
                }
            };
        });


        vm.commandProcessor(additionCommands);
        vm.commandProcessor(removalCommands);
    }

}


controller.$inject = [
    '$state'
];


const component = {
    template,
    bindings,
    controller
};


export default component;
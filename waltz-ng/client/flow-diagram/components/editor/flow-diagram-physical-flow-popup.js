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
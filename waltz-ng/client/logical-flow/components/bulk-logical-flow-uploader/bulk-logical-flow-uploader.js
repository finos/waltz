/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017  Waltz open source project
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
import { CORE_API } from '../../../common/services/core-api-utils';
import { nest } from 'd3-collection';

import { initialiseData } from '../../../common';
import { refToString } from '../../../common/entity-utils';


import template from './bulk-logical-flow-uploader.html';


const bindings = {
    flows: '<',
    onUploadComplete: '<'
};


const initialState = {
    loading: false,

    onUploadComplete: (event) => console.log('default onUploadComplete handler for bulk-logical-flow-uploader: ', event)
};


async function findExistingLogicalFlows(serviceBroker, sourcesAndTargets, force = false) {
    return serviceBroker
        .loadViewData(
            CORE_API.LogicalFlowStore.findBySourceAndTargetEntityReferences,
            [sourcesAndTargets],
            { force })
        .then(r => r.data)
}


async function findOrAddLogicalFlows(serviceBroker, sourceTargets = []) {
    if(_.isEmpty(sourceTargets)) {
        return sourceTargets;
    }

    // retrieves logical flows by source & target refs
    const existingLogicalFlows = await findExistingLogicalFlows(serviceBroker, sourceTargets);

    //compare with flows and add new ones
    const existingFlowsBySourceByTarget = nest()
        .key(flow => refToString(flow.source))
        .key(flow => refToString(flow.target))
        .object(existingLogicalFlows);

    const logicalFlowsToAdd = _.filter(sourceTargets, f => {
        const sourceRefString = refToString(f.source);
        const targetRefString = refToString(f.target);
        return _.get(existingFlowsBySourceByTarget, `[${sourceRefString}][${targetRefString}]`) === undefined;
    });

    const addFlowCmds = _.map(logicalFlowsToAdd, p => ({source: p.source, target: p.target}));
    console.log('logical flow add cmds: ', addFlowCmds);

    let allServerLogicalFlows = existingLogicalFlows;
    if(! _.isEmpty(addFlowCmds)) {
        const addedFlows = await serviceBroker
            .execute(CORE_API.LogicalFlowStore.addFlows, [addFlowCmds])
            .then(r => r.data);

        console.log('added flows: ', addedFlows);
        allServerLogicalFlows = _.union(existingLogicalFlows, addedFlows);
    }

    console.log('allServerFlows: ', allServerLogicalFlows);
    return allServerLogicalFlows;
}

async function updateDecorators(serviceBroker, existingLogicalFlows, newFlows) {
    // get the decorators that need to be added by flow id
    const flowsBySourceByTarget = nest()
        .key(flow => refToString(flow.source))
        .key(flow => refToString(flow.target))
        .object(newFlows);

    console.log('flowsBySourceByTarget: ', flowsBySourceByTarget);

    const updateCmds = _.map(existingLogicalFlows, f => {
        const sourceRefString = refToString(f.source);
        const targetRefString = refToString(f.target);
        const dataTypeRefs = _
            .chain(flowsBySourceByTarget)
            .get(`[${sourceRefString}][${targetRefString}]`)
            .map(f => f.dataType)
            .value();

        return {
            flowId: f.id,
            addedDecorators: dataTypeRefs,
            removedDecorators: []
        };
    });

    console.log('updateCmds: ', updateCmds);
    return serviceBroker
        .execute(CORE_API.LogicalFlowDecoratorStore.updateDecoratorsBatch, [updateCmds])
        .then(r => r.data);
}


function controller(serviceBroker) {
    const vm = initialiseData(this, initialState);

    const uploadFlows = () => {
        if(vm.flows) {
            vm.loading = true;
            findOrAddLogicalFlows(serviceBroker, vm.flows)
                .then(logicalFlows => updateDecorators(serviceBroker, logicalFlows, vm.flows))
                .then(decorators => vm.loading = false);
        }
    };


    vm.$onInit = () => {
    };

    vm.$onChanges = (changes) => {
        console.log('changes: ', changes)
        uploadFlows();
    };



}


controller.$inject = [
    'ServiceBroker'
];


const component = {
    template,
    bindings,
    controller
};


export default {
    component,
    id: 'waltzBulkLogicalFlowUploader'
};

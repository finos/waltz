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
import {initialiseData} from '../common';
import {kindToViewState} from '../common/link-utils';


import template from './physical-flow-registration.html';
import {CORE_API} from "../common/services/core-api-utils";
import {loadEntity} from "../common/entity-utils";
import {removeEnrichments} from "./physical-flow-utils";


const initialState = {
    cancelLink: '#/',
    existingSpecifications: [],
    flowAttributes: null,
    sourceEntity: null,
    specification: null,
    targetLogicalFlow: null,
    existingTargets: [],
    existingFlowsByTarget: {},
    similarFlows: [],
    validation: {
        messages: [],
        canSave: false
    },
    visibility: {
        editor: "",
        loading: false,
        similarFlows: false
    }
};


function validate(specification, flowAttributes, targetLogicalFlow) {

    const messages = [];
    messages.push(specification ? null : "Missing specification. ");
    messages.push(flowAttributes ? null : "Missing attributes. ");
    messages.push(targetLogicalFlow ? null : "Missing target. ");

    return {
        canSave: specification && flowAttributes && targetLogicalFlow,
        messages: _.reject(messages, m => m == null)
    };
}


function toAttributes(physFlow) {
    return {
        transport: physFlow.transport,
        frequency: physFlow.frequency,
        criticality: physFlow.criticality,
        basisOffset: physFlow.basisOffset
    };
}


function controller(
    $scope,
    $state,
    $stateParams,
    notification,
    serviceBroker,
    preventNavigationService) {

    const vm = initialiseData(this, initialState);
    preventNavigationService.setupWarningDialog($scope, () => isDirty());

    vm.similarFlowDefs = [
        { field: 'specification.name', displayName: 'Name', width: "10%" },
        { field: 'specification.format', displayName: 'Format', width: "10%", cellFilter: 'toDisplayName:"dataFormatKind"' },
        { field: 'physicalFlow.transport', displayName: 'Transport', width: "14%", cellFilter: 'toDisplayName:"transportKind"' },
        { field: 'physicalFlow.frequency', displayName: 'Frequency', width: "10%", cellFilter: 'toDisplayName:"frequencyKind"' },
        { field: 'physicalFlow.basisOffset', displayName: 'Basis', width: "10%", cellFilter: 'toBasisOffset' },
        { field: 'specification.description', displayName: 'Description' }

    ];


    const sourceEntityRef = {
        id: $stateParams.id,
        kind: $stateParams.kind
    };

    const targetFlowId = $stateParams.targetLogicalFlowId;

    if (targetFlowId) {
        serviceBroker
            .loadViewData(CORE_API.LogicalFlowStore.getById, [ targetFlowId ])
            .then(r => vm.targetChanged(r.data));
    }

    const viewState = kindToViewState(sourceEntityRef.kind);
    vm.cancelLink = $state.href(viewState, { id: sourceEntityRef.id });

    const doValidation = () => {
        vm.visibility.similarFlows = false;
        // vm.similarFlows = findSimilarFlows(vm.specification, vm.flowAttributes, vm.targetEntity, vm.existingFlowsByTarget);
        return validate(vm.specification, vm.flowAttributes, vm.targetLogicalFlow);
    };

    const isDirty = () => {
        return vm.specification || vm.flowAttributes || vm.targetLogicalFlow;
    };

    vm.focusSpecification = () => {
        vm.visibility.editor = 'SPECIFICATION';
    };

    vm.focusFlowAttributes = () => {
        vm.visibility.editor = 'FLOW-ATTRIBUTES';
    };

    vm.focusTarget = () => {
        vm.visibility.editor = 'TARGET-LOGICAL-FLOW';
    };

    vm.focusClone = () => {
        vm.visibility.editor = 'CLONE';
        vm.visibility.loading = true;
        console.log('focusClone', { vm })
    };

    vm.attributesChanged = (attributes) => {
        vm.flowAttributes = attributes;
        vm.editorDismiss();
    };

    vm.targetChanged = (logicalFlow) => {
        vm.targetLogicalFlow = logicalFlow;
        vm.editorDismiss();
    };


    vm.specificationChanged = (spec) => {
        vm.specification = spec;
        vm.editorDismiss();
    };

    vm.editorDismiss = () => {
        vm.visibility.editor = '';
        vm.validation = doValidation();
    };

    vm.onClone = flow => {
        vm.specification = removeEnrichments(flow.specification);
        vm.flowAttributes = toAttributes(flow.physical);
        vm.targetLogicalFlow = flow.logical;
        vm.editorDismiss();
        notification.info("Flow has been cloned, please make some changes before saving.")
    };

    vm.doSave = () => {
        const validationResult = doValidation();
        if (validationResult.canSave) {
            const cmd = {
                specification: vm.specification,
                flowAttributes: vm.flowAttributes,
                logicalFlowId: vm.targetLogicalFlow.id
            };
            serviceBroker
                .execute(CORE_API.PhysicalFlowStore.create, [cmd])
                .then(r => {
                    const resp = r.data;
                    if(resp.outcome == 'SUCCESS') {
                        notification.info("Created new flow");
                    } else if(resp.outcome == 'FAILURE') {
                        notification.warning(resp.message + ", redirected to existing.")
                    }
                    if(resp.entityReference) {
                        //clear variables
                        vm.specification = null;
                        vm.flowAttributes = null;
                        vm.targetLogicalFlow = null;
                        $state.go('main.physical-flow.view', {id: resp.entityReference.id});
                    }
                });
        } else {
            const messages =  _.join(validationResult.messages, '<br> - ');
            notification.warning("Cannot save: <br> - " + messages);
        }
    };


    loadEntity(serviceBroker, sourceEntityRef)
        .then(ent => vm.sourceEntity = ent);

    serviceBroker
        .loadViewData(CORE_API.PhysicalSpecificationStore.findByEntityReference, [sourceEntityRef])
        .then(r =>  vm.existingSpecifications = r.data);

    serviceBroker
        .loadViewData(CORE_API.LogicalFlowStore.findByEntityReference, [ sourceEntityRef ])
        .then(r =>
            vm.outboundLogicalFlows = _
                .chain(r.data)
                .filter(f => f.source.kind === sourceEntityRef.kind && f.source.id === sourceEntityRef.id)
                .orderBy('target.name')
                .value());
}


controller.$inject = [
    '$scope',
    '$state',
    '$stateParams',
    'Notification',
    'ServiceBroker',
    'PreventNavigationService'
];


export default {
    template,
    controller,
    controllerAs: 'ctrl'
};

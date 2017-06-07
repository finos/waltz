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
import {initialiseData} from '../common';
import {kindToViewState} from '../common/link-utils';


import template from './physical-flow-registration.html';


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
        similarFlows: false
    }
};


function loadEntity(entityRef, appStore, actorStore) {
    switch (entityRef.kind) {
        case 'APPLICATION':
            return appStore
                .getById(entityRef.id)
                .then(app => Object.assign({}, app, { kind: 'APPLICATION' }));
        case 'ACTOR':
            return actorStore
                .getById(entityRef.id)
                .then(actor => Object.assign({}, actor, { kind: 'ACTOR' }));
        default:
            throw "Unsupported owningEntity (kind): " + entityRef;
    }
}


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


function findSimilarFlows(specification, flowAttributes, targetEntity, existingFlowsByTarget = {}) {
    if(specification && flowAttributes && targetEntity) {
        const existingFlows = existingFlowsByTarget[targetEntity.kind + "_" + targetEntity.id] || [];
        return _.chain(existingFlows)
                .filter(f => f.specificationId === specification.id)
                .map(physicalFlow => ({specification, physicalFlow}))
                .value();
    }
    return [];
}


function controller(
    $scope,
    $state,
    $stateParams,
    actorStore,
    applicationStore,
    logicalFlowStore,
    notification,
    physicalFlowStore,
    specificationStore,
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
        logicalFlowStore
            .getById(targetFlowId)
            .then(logicalFlow => vm.targetChanged(logicalFlow));
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


    vm.doSave = () => {
        const validationResult = doValidation();
        if (validationResult.canSave) {
            const cmd = {
                specification: vm.specification,
                flowAttributes: vm.flowAttributes,
                logicalFlowId: vm.targetLogicalFlow.id
            };
            physicalFlowStore
                .create(cmd)
                .then(resp => {
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
                })
        } else {
            const messages =  _.join(validationResult.messages, '<br> - ');
            notification.warning("Cannot save: <br> - " + messages);
        }
    };


    loadEntity(sourceEntityRef, applicationStore, actorStore)
        .then(ent => vm.sourceEntity = ent);

    specificationStore
        .findByEntityReference(sourceEntityRef)
        .then(specs => vm.existingSpecifications = specs);

    logicalFlowStore
        .findByEntityReference(sourceEntityRef)
        .then(flows =>
            vm.outboundLogicalFlows = _
                .chain(flows)
                .filter(f => f.source.kind === sourceEntityRef.kind && f.source.id === sourceEntityRef.id)
                .orderBy('target.name')
                .value());

}


controller.$inject = [
    '$scope',
    '$state',
    '$stateParams',
    'ActorStore',
    'ApplicationStore',
    'LogicalFlowStore',
    'Notification',
    'PhysicalFlowStore',
    'PhysicalSpecificationStore',
    'PreventNavigationService'
];


export default {
    template,
    controller,
    controllerAs: 'ctrl'
};

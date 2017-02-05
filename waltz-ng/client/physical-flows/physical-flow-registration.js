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


const template = require('./physical-flow-registration.html');


const initialState = {
    cancelLink: '#/',
    candidateSpecifications: [],
    flowAttributes: null,
    sourceEntity: null,
    specification: null,
    targetEntity: null,
    existingTargets: [],
    validation: {
        messages: [],
        canSave: false
    },
    visibility: {
        editor: ""
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
            throw "Unsupported owningEntity (kind): " + entityRef
    }
}


function validate(specification, flowAttributes, targetEntity) {

    const messages = [];
    messages.push(specification ? null : "Missing specification. ");
    messages.push(flowAttributes ? null : "Missing attributes. ");
    messages.push(targetEntity ? null : "Missing target. ");

    return {
        canSave: specification && flowAttributes && targetEntity,
        messages: _.reject(messages, m => m == null)
    };
}


function controller(
    $q,
    $state,
    $stateParams,
    actorStore,
    applicationStore,
    logicalFlowStore,
    notification,
    physicalFlowStore,
    specificationStore) {

    const vm = initialiseData(this, initialState);

    const sourceEntityRef = {
        id: $stateParams.id,
        kind: $stateParams.kind
    };

    const viewState = kindToViewState(sourceEntityRef.kind);
    vm.cancelLink = $state.href(viewState, { id: sourceEntityRef.id });

    const doValidation = () => validate(vm.specification, vm.flowAttributes, vm.targetEntity);

    vm.focusSpecification = () => {
        vm.visibility.editor = 'SPECIFICATION';
    };

    vm.focusFlowAttributes = () => {
        vm.visibility.editor = 'FLOW-ATTRIBUTES';
    };

    vm.focusTarget = () => {
        vm.visibility.editor = 'TARGET-ENTITY';
    };

    vm.attributesChanged = (attributes) => {
        vm.flowAttributes = attributes;
        vm.editorDismiss();
    };

    vm.targetChanged = (target) => {
        vm.targetEntity = target;
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
                targetEntity: { id: vm.targetEntity.id, kind: vm.targetEntity.kind }
            };
            physicalFlowStore
                .create(cmd)
                .then(resp => {
                    notification.info("Created new flow");
                    $state.go('main.physical-flow.view', {id: resp.entityReference.id });
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
        .then(specs => vm.candidateSpecifications = specs);


    const existingLogicalTargetPromise = logicalFlowStore
        .findByEntityReference(sourceEntityRef.kind, sourceEntityRef.id)
        .then(flows => _.chain(flows)
            .map('target')
            .uniqBy(r => r.kind + r.id)
            .value());

    const existingPhysicalTargetPromise = physicalFlowStore
        .findByEntityReference(sourceEntityRef)
        .then(flows => _.chain(flows)
            .map('target')
            .uniqBy(r => r.kind + r.id)
            .value());

    $q.all([existingLogicalTargetPromise, existingPhysicalTargetPromise])
        .then(([logical = [], physical = []]) =>
            _.chain(logical)
                .concat(physical)
                .uniqBy(r => r.kind + r.id).value())
        .then(targets => vm.existingTargets = targets);

}


controller.$inject = [
    '$q',
    '$state',
    '$stateParams',
    'ActorStore',
    'ApplicationStore',
    'LogicalFlowStore',
    'Notification',
    'PhysicalFlowStore',
    'PhysicalSpecificationStore',
];


export default {
    template,
    controller,
    controllerAs: 'ctrl'
};

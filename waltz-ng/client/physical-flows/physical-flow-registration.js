import _ from 'lodash';
import {initialiseData, kindToViewState} from '../common';


const template = require('./physical-flow-registration.html');


const initialState = {
    sourceEntity: null,
    candidateSpecifications: [],
    specification: null,
    flowAttributes: null,
    targetEntity: null,
    cancelLink: '#/flibber',
    visibility: {
        editor: ""
    },
    validation: {
        messages: [],
        canSave: false
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
    $state,
    $stateParams,
    actorStore,
    applicationStore,
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
}


controller.$inject = [
    '$state',
    '$stateParams',
    'ActorStore',
    'ApplicationStore',
    'Notification',
    'PhysicalFlowStore',
    'PhysicalSpecificationStore',
];


export default {
    template,
    controller,
    controllerAs: 'ctrl'
};

import {initialiseData} from '../common';


const template = require('./physical-flow-registration.html');


const initialState = {
    owningEntity: null,
    candidateSpecifications: [],
    logicalFlows: [],
    physicalFlows: [],


    visibility: {
        targetFocused: false,
        specificationFocused: false,
        attributesFocused: false
    }
};


function updateSpecification(registration, specification) {
    return Object.assign({}, registration, { specification });
}


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


function controller(
    $stateParams,
    actorStore,
    applicationStore,
    logicalFlowStore,  // DataFlowDataStore
    physicalFlowStore,
    specificationStore) {

    const vm = initialiseData(this, initialState);

    const owningEntityRef = {
        id: $stateParams.id,
        kind: $stateParams.kind
    };

    loadEntity(owningEntityRef, applicationStore, actorStore)
        .then(ent => vm.owningEntity = ent);

    specificationStore
        .findByEntityReference(owningEntityRef)
        .then(specs => vm.candidateSpecifications = specs);
}


controller.$inject = [
    '$stateParams',
    'ActorStore',
    'ApplicationStore',
    'DataFlowDataStore',  // LogicalFlowStore
    'PhysicalFlowStore',
    'PhysicalSpecificationStore',
];


export default {
    template,
    controller,
    controllerAs: 'ctrl'
};

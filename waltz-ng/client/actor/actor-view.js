const template = require('./actor-view.html');


function controller($stateParams,
                    actorStore,
                    physicalFlowStore,
                    physicalSpecificationStore) {

    const id = $stateParams.id;
    const ref = { kind: 'ACTOR', id };

    const vm = this;

    actorStore
        .getById(id)
        .then(a => vm.actor = a);

    physicalFlowStore
        .findByEntityReference(ref)
        .then(flows => vm.physicalFlows = flows);

    physicalSpecificationStore
        .findByEntityReference(ref)
        .then(specs => vm.physicalSpecifications = specs);

}


controller.$inject = [
    '$stateParams',
    'ActorStore',
    'PhysicalFlowStore',
    'PhysicalSpecificationStore'
];


const view = {
    template,
    controller,
    controllerAs: 'ctrl'
};

export default view;
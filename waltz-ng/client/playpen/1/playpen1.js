
const initData = {
};




function controller($q, dataFlowStore, dataFlowDecoratorStore, physicalSpecificationStore, physicalFlowStore) {
    const vm = Object.assign(this, initData);

    const appId = 66766;
    const ref = {
        kind: 'APPLICATION',
        id: appId
    };

    const selector = {
        entityReference: ref,
        scope: 'EXACT'
    };

    physicalSpecificationStore
        .findByAppId(appId)
        .then(xs => vm.specifications = xs);

    dataFlowStore
        .findByEntityReference(ref)
        .then(xs => vm.logicalFlows = xs);

    physicalFlowStore
        .findByEntityReference(ref)
        .then(xs => vm.physicalFlows = xs);

    dataFlowDecoratorStore
        .findBySelector(selector)
        .then(xs => vm.dataFlowDecorators = xs);

    vm.entityRef = ref;
}


controller.$inject = [
    '$q',
    'DataFlowDataStore',
    'DataFlowDecoratorStore',
    'PhysicalSpecificationStore',
    'PhysicalFlowStore'
];


const view = {
    template: require('./playpen1.html'),
    controller,
    controllerAs: 'ctrl',
    bindToController: true,
    scope: {}
};


export default view;
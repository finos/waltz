
const initData = {
    dataTypes: []
};


function controller($q,
                    appStore,
                    dataFlowStore,
                    dataFlowDecoratorStore,
                    dataTypeStore) {

    const vm = Object.assign(this, initData);

    const entityRef = {
        id: 2440,
        kind: 'APPLICATION'
    };

    const selector = {
        entityReference: entityRef,
        scope: 'EXACT'
    };

    vm.entityReference = entityRef;

    $q.all([
        appStore.getById(entityRef.id),
        dataFlowStore.findByEntityReference(entityRef.kind, entityRef.id),
        dataFlowDecoratorStore.findBySelectorAndKind(selector, 'DATA_TYPE'),
        dataTypeStore.findAll()

    ]).then(([app, flows, decorators, dataTypes]) => {
        vm.app = app;
        vm.flows = flows;
        vm.decorators = decorators;
        vm.dataTypes = dataTypes;

    });
}


controller.$inject = [
    '$q',
    'ApplicationStore',
    'DataFlowDataStore',
    'DataFlowDecoratorStore',
    'DataTypeStore'
];


const view = {
    template: require('./playpen2.html'),
    controller,
    controllerAs: 'ctrl',
    bindToController: true,
    scope: {}
};


export default view;

const initData = {
    apps: [],
    flowData: null
};




function controller(appStore, dataFlowStore, dataFlowDecoratorStore) {

    const vm = Object.assign(this, initData);

    const entityReference = {
        id: 6591,
        kind: 'APPLICATION'
    };

    const selector = {
        entityReference,
        scope: 'EXACT'
    };

    dataFlowStore
        .findByEntityReference(entityReference)
        .then(f => vm.flows = f)
        .then(t => console.log(t));
    dataFlowDecoratorStore
        .findBySelector(selector)
        .then(d => vm.decorators = d);
    vm.entityReference = entityReference;

}


controller.$inject = [
    'ApplicationStore',
    'DataFlowDataStore',
    'DataFlowDecoratorStore'
];


const view = {
    template: require('./playpen1.html'),
    controller,
    controllerAs: 'ctrl',
    bindToController: true,
    scope: {}
};


export default view;
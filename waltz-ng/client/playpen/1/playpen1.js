
const initData = {
    apps: [],
    flowData: null
};




function controller(appStore, flowService) {

    const vm = Object.assign(this, initData);

    const entityReference = {
        id: 5000,
        kind: 'DATA_TYPE'
    };

    const selector = {
        entityReference,
        scope: 'CHILDREN'
    };

    appStore
        .findBySelector(selector)
        .then(apps => vm.apps = apps);

    flowService
        .initialise(selector)
        .then(() => flowService.loadDetail())
        .then(flowData => {
            vm.flowData = flowData;
        });

    vm.entityReference = entityReference;

}


controller.$inject = [
    'ApplicationStore',
    'DataFlowViewService'
];


const view = {
    template: require('./playpen1.html'),
    controller,
    controllerAs: 'ctrl',
    bindToController: true,
    scope: {}
};


export default view;
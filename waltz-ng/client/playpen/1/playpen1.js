


const initData = {
    apps: [],
    flowData: null
};



function controller(appStore, flowService) {

    const vm = Object.assign(this, initData);

    const entityReference = {
        id: 260,
        kind: 'ORG_UNIT'
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
        .then(fd => vm.flowData = fd);

    vm.onLoadDetail = () => {
        flowService
            .loadDetail()
            .then(fd => vm.flowData = fd);
    };
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
const initData = {
    applications: [],
    visibility: {}
};


function controller($q,
                    $stateParams,
                    appStore,
                    dataFlowViewService) {

    const vm = Object.assign(this, initData);

    const appIdSelector = {
        entityReference : {
            id: $stateParams.id,
            kind: $stateParams.kind
        },
        scope: "CHILDREN"
    };


    // -- LOAD

    appStore
        .findBySelector(appIdSelector)
        .then(apps => vm.applications = apps);

    dataFlowViewService.initialise(appIdSelector.entityReference.id, appIdSelector.entityReference.kind)
        .then(flows => vm.dataFlows = flows);


    vm.loadFlowDetail = () => dataFlowViewService.loadDetail();


    global.vm = vm;
}


controller.$inject = [
    '$q',
    '$stateParams',
    'ApplicationStore',
    'DataFlowViewService'
];


const view = {
    template: require('./playpen.html'),
    controller,
    controllerAs: 'ctrl',
    bindToController: true,
    scope: {}
};


export default view;
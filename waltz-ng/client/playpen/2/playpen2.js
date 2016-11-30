const initData = {
    rating: 'R'
};


function controller(notification,
                    applicationStore,
                    logicalFlowViewService) {

    const vm = Object.assign(this, initData);

    const id = 1

    applicationStore
        .findBySelector({ entityReference: { id, kind: 'APP_GROUP' }, scope: 'EXACT' })
        .then(apps => vm.applications = apps);

    logicalFlowViewService
        .initialise(id, 'APP_GROUP', 'EXACT')
        .then(logicalFlowViewService.loadDetail())
        .then(flowData => vm.dataFlows = flowData);


}


controller.$inject = [
    'Notification',
    'ApplicationStore',
    'LogicalFlowViewService',
];


const view = {
    template: require('./playpen2.html'),
    controller,
    controllerAs: 'ctrl',
    bindToController: true,
    scope: {}
};


export default view;


const initData = {
    apps: [],
    flowData: null
};


function controller(appStore, flowViewService) {

    const vm = Object.assign(this, initData);

    const entityReference = {
        id: 170,
        kind: 'ORG_UNIT'
    };

    const selector = {
        entityReference,
        scope: 'CHILDREN'
    };

    appStore
        .findBySelector(selector)
        .then(apps => vm.applications = apps);

    flowViewService
        .initialise(selector)
        .then(flowViewService.loadDetail)
        .then(d => vm.flowData = d);

    vm.loadDetail = () => {};

    vm.options = {
        graphTweakers: {
            node: {
                enter: (selection) => selection.on('click.app-click', d => console.log(d)),
                update: _.identity,
                exit: _.identity
            }
        }
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
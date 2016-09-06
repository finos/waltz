
const initData = {
    dataTypes: []
};


function controller(appStore, dataTypeService, dataFlowService) {

    const vm = Object.assign(this, initData);

    const entityRef = {
        id: 260, // 50 FO, 260 FO - IT, 270 Eq IT
        kind: 'ORG_UNIT'
    };

    const selector = {
        entityReference: entityRef,
        scope: 'CHILDREN'
    };

    dataFlowService
        .initialise(selector)
        .then(flowData => vm.flowData = flowData);

    dataTypeService
        .loadDataTypes()
        .then(dts => vm.dataTypes = dts);

    vm.loadFlowDetail = () => dataFlowService
        .loadDetail()
        .then(flowData => vm.flowData = flowData);

    vm.entityReference = entityRef;

    appStore
        .findBySelector(selector)
        .then(apps => vm.apps = apps);
}


controller.$inject = [
    'ApplicationStore',
    'DataTypeService',
    'DataFlowViewService'
];


const view = {
    template: require('./playpen2.html'),
    controller,
    controllerAs: 'ctrl',
    bindToController: true,
    scope: {}
};


export default view;
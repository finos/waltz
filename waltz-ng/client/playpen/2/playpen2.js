
const initData = {
    dataTypes: []
};


function controller(appStore, dataTypeService, dataTypeUsageStore) {

    const vm = Object.assign(this, initData);

    const entityRef = {
        id: 5000,
        kind: 'DATA_TYPE'
    };

    const selector = {
        entityReference: entityRef,
        scope: 'CHILDREN'
    };

    vm.entityReference = entityRef;

    dataTypeUsageStore
        .findUsageStatsForDataTypeSelector(selector)
        .then(usageStats => vm.usageStats = usageStats);

}


controller.$inject = [
    'ApplicationStore',
    'DataTypeService',
    'DataTypeUsageStore'
];


const view = {
    template: require('./playpen2.html'),
    controller,
    controllerAs: 'ctrl',
    bindToController: true,
    scope: {}
};


export default view;
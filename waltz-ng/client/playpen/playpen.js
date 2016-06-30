const initData = {
    usages: [],
    visibility: {}
};


function controller($q,
                    $stateParams,
                    appStore,
                    dataFlowStore,
                    dataTypeStore,
                    dataTypeUsageStore,
                    notification) {

    const vm = Object.assign(this, initData);

    const entityRef = {
        id: $stateParams.id,
        kind: $stateParams.kind
    };

    vm.entityRef = entityRef;


    // -- LOAD

    appStore
        .getById(1896)
        .then(app => vm.counterpart = app);

    appStore
        .getById(entityRef.id)
        .then(app => vm.app = app);

    dataFlowStore
        .findByEntityReference(entityRef.kind, entityRef.id)
        .then(fs => vm.currentDataTypes = _.chain(fs)
            .filter(f => f.source.id === 1896)
            .map('dataType')
            .value());

    dataTypeStore
        .findAll()
        .then(xs => vm.allDataTypes = xs);


    dataTypeUsageStore
        .findForEntity(entityRef.kind, entityRef.id)
        .then(usages => vm.usages = usages);

    vm.save = (command) =>  dataFlowStore.create(command)
        .then((r) => console.log(r))
        .then(() => notification.success('Logical flows updated'));

    vm.cancel = () => console.log('Cancelled');

    global.vm = vm;
}


controller.$inject = [
    '$q',
    '$stateParams',
    'ApplicationStore',
    'DataFlowDataStore',
    'DataTypesDataService',
    'DataTypeUsageStore',
    'Notification'
];


const view = {
    template: require('./playpen.html'),
    controller,
    controllerAs: 'ctrl',
    bindToController: true,
    scope: {}
};


export default view;
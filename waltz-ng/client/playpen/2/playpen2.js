const initData = {
    dataTypes: [],
    checkedItemIds: [1000, 3000, 6002, 6310],
    expandedItemIds: []
};


function controller(dataTypeService,
                    dataFlowStore) {

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


    const loadData = () => {
        dataTypeService.loadDataTypes()
            .then(dataTypes => _.map(dataTypes, d => ({...d, selectable: d.name !== 'Ref Data'})))
            .then(dataTypes => {
                vm.expandedItemIds = _.union(vm.checkedItemIds);
                vm.dataTypes = dataTypes;
            });
    };

    loadData();

    vm.nodeSelected = (id) => console.log('selected: ', id);

    vm.nodeChecked = (id) => {
        console.log('checked: ', id);
        vm.checkedItemIds = _.union(vm.checkedItemIds, [id])
    };

    vm.nodeUnchecked = (id) => {
        console.log('unchecked: ', id);
        vm.checkedItemIds = _.without(vm.checkedItemIds, id);
    };

    vm.search = (query) => {
        console.log("searching: ", query);
    }

}


controller.$inject = [
    'DataTypeService',
    'DataFlowDataStore'
];


const view = {
    template: require('./playpen2.html'),
    controller,
    controllerAs: 'ctrl',
    bindToController: true,
    scope: {}
};


export default view;
import {prepareDataTypeTree} from "../../data-types/utilities";

const initData = {
    dataTypes: [],
    checkedDataTypes: []
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
            .then(dataTypes => _.map(dataTypes, d => ({...d, checked: false})))
            .then(dataTypes => vm.dataTypes = dataTypes)
            .then(dataTypes => dataFlowStore.countByDataType())
            .then(tallies => vm.trees = prepareDataTypeTree(vm.dataTypes, tallies));
    };

    loadData();

    vm.nodeHighlighted = (node) => console.log('highlighted: ', node)
    vm.nodeSelected = (node) => console.log('selected: ', node, vm.checkedDataTypes)
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
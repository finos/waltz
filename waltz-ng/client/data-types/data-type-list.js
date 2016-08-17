import angular from "angular";
import {initialiseData} from "../common";
import {prepareDataTypeTree} from "./utilities";


const initialState = {
    dataTypes: [],
    tallies: [],
    trees: []
};


function controller($state,
                    dataFlowStore,
                    dataTypes,
                    staticPanelStore,
                    svgStore) {

    const vm = initialiseData(this, initialState);

    vm.dataTypes = dataTypes;

    vm.nodeSelected = (node) => vm.selectedNode = node;

    svgStore
        .findByKind('DATA_TYPE')
        .then(xs => vm.diagrams = xs);

    staticPanelStore
        .findByGroup("HOME.DATA-TYPE")
        .then(panels => vm.panels = panels);

    vm.blockProcessor = b => {
        b.block.onclick = () => $state.go('main.data-type.view', { dataTypeId: b.value });
        angular.element(b.block).addClass('clickable');
    };

    dataFlowStore.countByDataType()
        .then(tallies => vm.tallies = tallies )
        .then(tallies => vm.trees = prepareDataTypeTree(vm.dataTypes, vm.tallies));

}


controller.$inject = [
    '$state',
    'DataFlowDataStore',
    'dataTypes',
    'StaticPanelStore',
    'SvgDiagramStore'
];


const view = {
    template: require('./data-type-list.html'),
    controllerAs: 'ctrl',
    controller
};


export default view;

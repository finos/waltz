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
                    dataFlowUtilityService,
                    dataTypes,
                    displayNameService,
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
        b.block.onclick = () => $state.go('main.data-type.code', { code: b.value });
        angular.element(b.block).addClass('clickable');
    };

    dataFlowStore.countByDataType()
        .then(tallies => vm.tallies = dataFlowUtilityService.enrichDataTypeCounts(tallies, displayNameService))
        .then(tallies => vm.trees = prepareDataTypeTree(vm.dataTypes, vm.tallies));

}


controller.$inject = [
    '$state',
    'DataFlowDataStore',
    'DataFlowUtilityService',
    'dataTypes',
    'WaltzDisplayNameService',
    'StaticPanelStore',
    'SvgDiagramStore'
];


const view = {
    template: require('./data-type-list.html'),
    controllerAs: 'ctrl',
    controller
};


export default view;
import angular from "angular";
import {initialiseData, buildHierarchies} from "../common";

const initialState = {
    dataTypes: [],
    dataTypeHierarchy: []
};


function controller($state,
                    dataTypes,
                    staticPanelStore,
                    svgStore) {

    const vm = initialiseData(this, initialState);

    vm.dataTypes = dataTypes;
    vm.dataTypeHierarchy = buildHierarchies(dataTypes);

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
}


controller.$inject = [
    '$state',
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

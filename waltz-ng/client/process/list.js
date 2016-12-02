import {termSearch} from "../common";


const initialState = {
    processes: [],
    filteredProcesses: [],
    processQuery: ''
};


function controller($scope,
                    $state,
                    processStore,
                    sourceDataRatingStore,
                    staticPanelStore,
                    svgStore) {
    const vm = Object.assign(this, initialState);

    processStore
        .findAll()
        .then(ps => vm.processes = ps);

    sourceDataRatingStore
        .findAll()
        .then(sdrs => vm.sourceDataRatings = sdrs);

    svgStore
        .findByKind('PROCESS')
        .then(xs => vm.diagrams = xs);

    staticPanelStore
        .findByGroup("HOME.PROCESS")
        .then(panels => vm.panels = panels);

    vm.blockProcessor = b => {
        b.block.onclick = () => $state.go('main.process.view', { id: b.value });
        angular.element(b.block).addClass('clickable');
    };

    $scope.$watchGroup(
        ['ctrl.processQuery', 'ctrl.processes'],
        ([q, ps]) => vm.filteredProcesses = termSearch(ps, q)
    );


    vm.dismissSourceDataOverlay = () => {
        vm.visibility.sourceDataRatings = false;
    };

}


controller.$inject = [
    '$scope',
    '$state',
    'ProcessStore',
    'SourceDataRatingStore',
    'StaticPanelStore',
    'SvgDiagramStore'
];


const view = {
    template: require('./list.html'),
    controller,
    controllerAs: 'ctrl'
};


export default view;

const initialState = {
    processes: []
};


function controller($state,
                    processStore,
                    svgStore) {
    const vm = Object.assign(this, initialState);

    processStore
        .findAll()
        .then(ps => vm.processes = ps);


    svgStore.findByKind('PROCESS').then(xs => vm.diagrams = xs);

    vm.blockProcessor = b => {
        b.block.onclick = () => $state.go('main.process.view', { id: b.value });
        angular.element(b.block).addClass('clickable');
    };
}


controller.$inject = [
    '$state',
    'ProcessStore',
    'SvgDiagramStore'
];


const view = {
    template: require('./list.html'),
    controller,
    controllerAs: 'ctrl'
};


export default view;
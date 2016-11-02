const initData = {
    gridData: [],
    filteredGriData: []
};


function controller($interval, lineageStore) {

    const vm = Object.assign(this, initData);

    vm.currentAttributes = {
        basisOffset: "-25",
        frequency: "ON_DEMAND",
        transport: "OTHER",
    };

    vm.attributesChanged = (attributes) => {
        vm.currentAttributes = attributes;
    };

    vm.editorDismissed = () =>
    {
        vm.editorVisible = false
    };
}


controller.$inject = [
    '$interval',
    'PhysicalFlowLineageStore'
];


const view = {
    template: require('./playpen2.html'),
    controller,
    controllerAs: 'ctrl',
    bindToController: true,
    scope: {}
};


export default view;
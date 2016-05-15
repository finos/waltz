
function controller($scope, dataFlowStore) {
    const vm = this;

    dataFlowStore
        .calculateStats([817, 813, 812, 811, 1096, 990])
        .then(stats => vm.stats = stats);


}

controller.$inject = [
    '$scope', 'DataFlowDataStore'
];


export default {
    template: require('./playpen.html'),
    controller,
    controllerAs: 'ctrl',
    bindToController: true,
    scope: {}
};
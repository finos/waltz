

export default () => ({
    restrict: 'E',
    replace: true,
    template: require('./svg-diagrams.html'),
    scope: {},
    bindToController: {
        diagrams: '=',
        blockProcessor: '='
    },
    controllerAs: 'ctrl',
    controller: () => {}
});

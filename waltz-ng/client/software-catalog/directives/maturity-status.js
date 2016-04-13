const BINDINGS = {
    status: '='
};

function controller() {

}

controller.$inject = [];


export default () => ({
    restrict: 'E',
    replace: true,
    scope: {},
    template: require('./maturity-status.html'),
    bindToController: BINDINGS,
    controllerAs: 'ctrl',
    controller
});

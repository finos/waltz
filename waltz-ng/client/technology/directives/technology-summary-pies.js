const BINDINGS = {
    softwareCatalog: '=',
    servers: '=',
    databases: '=',
};


function controller() {

}

controller.$inject = [ '$scope' ];


export default () => ({
    restrict: 'E',
    replace: true,
    scope: {},
    template: require('./technology-summary-pies.html'),
    bindToController: BINDINGS,
    controllerAs: 'ctrl',
    controller
});

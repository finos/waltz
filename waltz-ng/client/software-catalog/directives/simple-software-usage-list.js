const BINDINGS = {
    usages: '=',
    packages: '='
};


function controller() {
}

controller.$inject = [ ];


export default () => ({
    restrict: 'E',
    replace: true,
    scope: {
        panel: '='
    },
    template: require('./simple-software-usage-list.html'),
    bindToController: BINDINGS,
    controllerAs: 'ctrl',
    controller
});

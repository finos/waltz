const BINDINGS = {
    statistic: '<',
    value: '<'
};


function controller() {
}


const directive = {
    restrict: 'E',
    replace: false,
    scope: {},
    bindToController: BINDINGS,
    controller,
    controllerAs: 'ctrl',
    template: require('./entity-statistic-numeric-renderer.html')
};


export default () => directive;

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
    template: require('./entity-statistic-boolean-renderer.html')
};


export default () => directive;

const BINDINGS = {
    statistic: '<',
    value: '<'
};


function controller() {
    const vm = this;

    vm.isNumber = (v) => !isNaN(parseFloat((v)));
}


const directive = {
    restrict: 'E',
    replace: false,
    scope: {},
    bindToController: BINDINGS,
    controller,
    controllerAs: 'ctrl',
    template: require('./entity-statistic-percentage-renderer.html')
};


export default () => directive;

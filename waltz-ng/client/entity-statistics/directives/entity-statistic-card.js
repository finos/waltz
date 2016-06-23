const BINDINGS = {
    category: '<',
    values: '<'
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
    template: require('./entity-statistic-card.html')
};


export default () => directive;

const BINDINGS = {
    hasAppInvolvements: '<',
    hasEndUserAppInvolvements: '<',
    charts: '<'
};


function controller() {
}


controller.$inject = [];


const directive = {
    restrict: 'E',
    replace: false,
    template: require('./app-summary.html'),
    controller,
    controllerAs: 'ctrl',
    scope: {},
    bindToController: BINDINGS
};


export default () => directive;
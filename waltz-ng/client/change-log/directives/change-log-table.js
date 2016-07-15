const BINDINGS = {
    entries: '='
};


const directive = {
    restrict: 'E',
    replace: false,
    scope: {},
    bindToController: BINDINGS,
    controllerAs: 'ctrl',
    controller: () => {},
    template: require('./change-log-table.html')
};


export default () => directive;

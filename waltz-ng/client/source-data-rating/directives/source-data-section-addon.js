const BINDINGS = {
    entities: '=',
    ratings: '='
};


const directive = {
    restrict: 'E',
    replace: true,
    controller: () => {},
    bindToController: BINDINGS,
    controllerAs: 'ctrl',
    scope: {},
    template: require('./source-data-section-addon.html')
};


export default () => directive;
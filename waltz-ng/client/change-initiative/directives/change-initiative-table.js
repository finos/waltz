const BINDINGS = {
    list: '='
};


function controller() {

}

const directive = {
    restrict: 'E',
    replace: true,
    scope: {},
    controller,
    controllerAs: 'ctrl',
    bindToController: BINDINGS,
    template: require('./change-initiative-table.html')
};


export default () => directive;
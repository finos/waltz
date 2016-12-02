const BINDINGS = {
    entities: '=',
    ratings: '='
};


function controller() {
    const vm = this;

    vm.dismissSourceDataOverlay = () => {
        vm.visible = false;
    }
}


const directive = {
    restrict: 'E',
    replace: true,
    controller,
    bindToController: BINDINGS,
    controllerAs: 'ctrl',
    scope: {},
    template: require('./source-data-section-addon.html')
};


export default () => directive;
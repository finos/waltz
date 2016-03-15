const BINDINGS = {
    applications: '=',
    ratings: '=',
    capabilities: '=',
    initiallySelectedIds: '=',
    explicitCapabilityIds: '=',
    capabilitySelectorVisible: '='
};

function controller() {

}


controller.$inject = ['$scope'];


export default () => ({
    replace: true,
    restrict: 'E',
    scope: {},
    bindToController: BINDINGS,
    controllerAs: 'ctrl',
    controller,
    template: require('./multi-app-rating-viewer.html')
});

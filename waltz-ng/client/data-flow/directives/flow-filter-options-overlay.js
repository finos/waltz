const BINDINGS = {
    visible: '=',
    onChange: '=',
    types: '='
};


const initialState = {
    selectedType: 'ALL',
    selectedScope: 'INTRA',
    visible: false,
    onChange: () => console.log('No change handler registered for flow-filter-options-overlay::onChange')
};


function controller($scope) {
    const vm = _.defaults(this, initialState);

    $scope.$watchGroup(
        ['ctrl.selectedType', 'ctrl.selectedScope'],
        ([type = 'ALL', scope ='INTRA']) => vm.onChange({ type, scope })
    );
}


controller.$inject = [
    '$scope'
];


const directive = {
    restrict: 'E',
    replace: true,
    controller,
    bindToController: BINDINGS,
    controllerAs: 'ctrl',
    scope: {},
    template: require('./flow-filter-options-overlay.html')
};


export default () => directive;
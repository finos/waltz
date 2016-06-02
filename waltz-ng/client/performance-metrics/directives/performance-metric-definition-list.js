const BINDINGS = {
    definitions: "="
};


const initialState = {
    groupedDefinitions: {}  // { categoryName -> [definitions] }
};


function groupDefinitions(definitions) {
    return _.chain(definitions)
        .orderBy('name')
        .groupBy('categoryName')
        .value();
}


function controller($scope) {

    const vm = Object.assign(this, initialState);

    $scope.$watch(
        'ctrl.definitions',
        (definitions = []) =>
            vm.groupedDefinitions = groupDefinitions(definitions));

}


controller.$inject = [
    '$scope'
];


const directive = {
    restrict: 'E',
    replace: true,
    template: require('./performance-metric-definition-list.html'),
    controller,
    controllerAs: 'ctrl',
    scope: {},
    bindToController: BINDINGS
};


export default () => directive;


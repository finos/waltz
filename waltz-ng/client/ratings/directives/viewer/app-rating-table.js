import d3 from "d3";


const BINDINGS = {
    applications: '=',
    ratings: '=',
    colorStrategy: '=',
    capabilities: '='
};


function controller($scope) {

    const vm = this;

    $scope.$watch('ctrl.ratings', (ratings => {
        if (!ratings) return;
        vm.ratingMap = d3.nest()
                .key(d => d.parent.id)
                .key(d => d.capability.id)
                .key(d => d.measurable.code)
                .map(ratings);
    }));

    vm.lookupCell = (appId, capId) => vm.colorStrategy.fn(appId, capId, vm.ratingMap);

}

controller.$inject = ['$scope'];


export default () => ({
    replace: true,
    restrict: 'E',
    scope: {},
    bindToController: BINDINGS,
    controllerAs: 'ctrl',
    controller,
    template: require('./app-rating-table.html')
});

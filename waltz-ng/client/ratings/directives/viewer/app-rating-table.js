import d3 from "d3";


const BINDINGS = {
    applications: '=',
    ratings: '=',
    colorStrategy: '=',
    capabilities: '='
};


/**
 * Convert raw ratings into a nested
 * structure similar to:
 *
 * { appId -> cabilityId -> measurable -> [rag] }
 *
 * Note that the final rag will always be a single value, however
 * d3.nest() will always wrap that in an array.
 *
 * @param ratings
 * @returns {*}
 */
function perpareRatingData(ratings) {
    return d3.nest()
        .key(d => d.parent.id)
        .key(d => d.capability.id)
        .key(d => d.measurable.code)
        .map(ratings);
}


function controller($scope) {
    const vm = this;

    $scope.$watch('ctrl.ratings', (ratings => {
        if (!ratings) return;
        vm.ratingMap = perpareRatingData(ratings);
    }));

    vm.determineRating = (appId, capId) => vm.colorStrategy.fn(appId, capId, vm.ratingMap);
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

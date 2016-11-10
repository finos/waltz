import _ from "lodash";

const BINDINGS = {
    data: '<',
    config: '<',
    title: '@',
    subTitle: '@',
    icon: '@',
    selectedSegmentKey: '<'
};

const MAX_PIE_SEGMENTS = 5;

function controller($scope) {

    const vm = this;

    const dataChanged = (data) => {
        if (!data) return;
        vm.total = _.sumBy(data, 'count');

        if (data.length > MAX_PIE_SEGMENTS) {
            const sorted = _.sortBy(data, d => d.count * -1);

            const topData = _.take(sorted, MAX_PIE_SEGMENTS);
            const otherData = _.drop(sorted, MAX_PIE_SEGMENTS);
            const otherDatum = {
                key: 'Other',
                count : _.sumBy(otherData, "count")
            };

            vm.pieData = _.concat(topData, otherDatum);
        } else {
            vm.pieData = data;
        }

    };

    $scope.$watch(
        'ctrl.data',
        dataChanged);

    vm.toDisplayName = (k) => vm.config.labelProvider
        ? vm.config.labelProvider(k)
        : k;
}

controller.$inject = ['$scope'];


export default () => {
    return {
        restrict: 'E',
        replace: true,
        template: require('./pie-table.html'),
        scope: {},
        bindToController: BINDINGS,
        controllerAs: 'ctrl',
        controller
    };
};

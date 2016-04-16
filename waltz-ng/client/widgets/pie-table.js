import _ from "lodash";


function controller($scope) {

    const vm = this;

    const dataChanged = (data) => {
        if (!data) return;
        vm.total = _.sumBy(data, 'count');
    };

    $scope.$watch('ctrl.data', dataChanged);

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
        bindToController: {
            data: '=',
            config: '=',
            title: '@',
            icon: '@'
        },
        controllerAs: 'ctrl',
        controller
    };
};

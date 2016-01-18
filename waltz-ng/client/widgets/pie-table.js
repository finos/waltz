import _ from 'lodash';


function controller($scope) {

    const vm = this;

    const dataChanged = (data) => {
        if (!data) return;
        vm.total = _.sum(data, 'count');
    };

    $scope.$watch('ctrl.data', dataChanged);
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
            title: '@'
        },
        controllerAs: 'ctrl',
        controller
    };
};

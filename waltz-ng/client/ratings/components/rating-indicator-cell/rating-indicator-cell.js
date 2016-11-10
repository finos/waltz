const bindings = {
    rating: '<',
    label: '<'
};


const template = require('./rating-indicator-cell.html');


function controller($scope) {
    const vm = this;

    // this needs to be $watch to work round ui-grid cell-template refresh issues
    $scope.$watch('$ctrl.rating', (r) => vm.clazz = `wric-${ r }`);
}


controller.$inject = ['$scope'];


const component = {
    template,
    bindings,
    controller
};


export default component;
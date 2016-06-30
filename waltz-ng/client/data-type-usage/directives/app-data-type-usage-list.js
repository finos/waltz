import _ from "lodash";
import d3 from "d3";

const BINDINGS = {
    usages: "<"
};


const initialState = {
    consolidatedUsages:{},
    usages: []
};


function consolidateUsages(usages = []) {
    return d3.nest()
        .key(u => u.dataTypeCode)
        .sortKeys(u => u.dataTypeCode)
        .entries(usages);
}


function controller($scope) {
    const vm = _.defaultsDeep(this, initialState);

    $scope.$watch(
        'ctrl.usages',
        (usages = []) => vm.consolidatedUsages = consolidateUsages(usages)
    );
}


controller.$inject = ['$scope'];


const directive = {
    restrict: 'E',
    replace: false,
    template: require('./app-data-type-usage-list.html'),
    controller,
    controllerAs: 'ctrl',
    scope: {},
    bindToController: BINDINGS
};


export default () => directive;


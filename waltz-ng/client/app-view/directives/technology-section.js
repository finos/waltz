import _ from "lodash";
import {termSearch} from "../../common";

const BINDINGS = {
    catalog: '='
};


const FIELDS_TO_SEARCH = ['vendor', 'name', 'version'];


function controller($scope) {

    const vm = this;


    $scope.$watchGroup(
        ['ctrl.qry', 'ctrl.catalog.packages'],
        ([qry, packages = []]) => {
            vm.filteredPackages = qry
                ? termSearch(packages, qry, FIELDS_TO_SEARCH)
                : packages;
        }
    );


    $scope.$watch(
        'ctrl.catalog.usages',
        (usages = []) => {
            vm.usages = _.chain(usages)
                .groupBy('provenance')
                .value();
        }
    );

}

controller.$inject = [ '$scope' ];


export default () => ({
    restrict: 'E',
    replace: true,
    scope: {
        panel: '='
    },
    template: require('./technology-section.html'),
    bindToController: BINDINGS,
    controllerAs: 'ctrl',
    controller
});

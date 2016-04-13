import _ from "lodash";
import {termSearch} from "../common";


function controller($scope, catalogStore) {
    const vm = this;

    const appIds = [891];

    catalogStore.findByAppIds(appIds)
        .then(resp => {
            vm.packages = resp.packages;
            vm.usages = resp.usages;
            vm.filteredPackages = vm.packages;
        });

    vm.isPrimary = (cId) => _.contains(vm.primaryCapabilityIds, cId);


    $scope.$watch(
        'ctrl.qry',
        (qry) => {
            vm.filteredPackages = qry
                ? termSearch(vm.packages, qry, ['vendor', 'name', 'version'])
                : vm.packages;
        }
    );

    // ---- modal


}

controller.$inject = [
    '$scope', 'SoftwareCatalogStore'
];


export default {
    template: require('./playpen.html'),
    controller,
    controllerAs: 'ctrl',
    bindToController: true,
    scope: {}
};
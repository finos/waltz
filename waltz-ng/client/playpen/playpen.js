import _ from "lodash";
import {termSearch} from "../common";
import {variableScale} from "../common/colors";

/*

 PLANNED,
 INVEST,
 HOLD,
 DISINVEST,
 UNSUPPORTED,
 RESTRICTED

 */
function prepareStats(items, usages) {

     return {
         maturity: _.chain(items)
            .countBy(item => item.maturityStatus)
            .map((count, key) => ({key, count}))
            .value(),
         vendor: _.chain(items)
             .countBy(item => item.vendor)
             .map((count, key) => ({key, count}))
             .value()
     };

}

function controller($scope, catalogStore) {
    const vm = this;

    const appIds = [891, 843, 813];

    catalogStore.findByAppIds(appIds)
        .then(resp => {
            vm.softwareCatalog = resp;
            vm.packages = resp.packages;
            vm.usages = resp.usages;
            vm.filteredPackages = vm.packages;
            recalcPieData();
        });

    vm.isPrimary = (cId) => _.contains(vm.primaryCapabilityIds, cId);

    vm.pieConfig = {
        maturity: {
            colorProvider: (d) => variableScale(d.data.key)
        },
        vendor: {
            colorProvider: (d) => variableScale(d.data.key)
        }
    };


    const recalcPieData = () => {
        vm.pieData = prepareStats(vm.filteredPackages, vm.usages);
    };

    $scope.$watch(
        'ctrl.qry',
        (qry) => {
            vm.filteredPackages = qry
                ? termSearch(vm.packages, qry, ['vendor', 'name', 'version'])
                : vm.packages;

            recalcPieData();
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
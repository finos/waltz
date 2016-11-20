import _ from "lodash";
import {maturityColorScale, variableScale} from "../../common/colors";


const BINDINGS = {
    usages: '=',
    packages: '='
};


const PIE_SIZE = 70;


function prepareStats(items = [], usages = []) {
    const usageCounts = _.countBy(usages, 'softwarePackageId');

    const countPieDataBy = (items = [], fn = (x => x)) =>
        _.chain(items)
            .groupBy(fn)
            .map((group, key) => {
                const calculatedCount = _.reduce(
                    group,
                    (acc, groupItem) => acc + usageCounts[groupItem.id] || 1,
                    0);
                return {
                    key,
                    count: calculatedCount
                };
            })
            .value();


    return {
        maturity: countPieDataBy(items, item => item.maturityStatus),
        vendor: countPieDataBy(items, item => item.vendor)
    };
}


function controller($scope) {

    const vm = this;

    vm.pieConfig = {
        maturity: {
            size: PIE_SIZE,
            colorProvider: (d) => { console.log(d); return maturityColorScale(d.data.key); }
        },
        vendor: {
            size: PIE_SIZE,
            colorProvider: (d) => variableScale(d.data.key)
        }
    };


    const recalcPieData = () => {
        vm.pieData = prepareStats(vm.packages, vm.usages);
    };


    $scope.$watchGroup(
        ['ctrl.packages', 'ctrl.usages'],
        () => recalcPieData()
    );

}

controller.$inject = [ '$scope' ];

export default () => ({
    restrict: 'E',
    replace: true,
    scope: {},
    template: require('./simple-software-usage-pies.html'),
    bindToController: BINDINGS,
    controllerAs: 'ctrl',
    controller
});

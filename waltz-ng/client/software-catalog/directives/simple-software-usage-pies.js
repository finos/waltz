import {maturityColorScale, variableScale} from "../../common/colors";

const BINDINGS = {
    usages: '=',
    packages: '='
};



function prepareStats(items = [], usages = []) {

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
function controller($scope) {

    const vm = this;

    vm.pieConfig = {
        maturity: {
            colorProvider: (d) => maturityColorScale(d.data.key)
        },
        vendor: {
            colorProvider: (d) => variableScale(d.data.key)
        }
    };


    const recalcPieData = () => {
        vm.pieData = prepareStats(vm.packages, vm.usages);
    };


    $scope.$watch(
        'ctrl.packages',
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

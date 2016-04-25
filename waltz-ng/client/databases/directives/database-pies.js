import {environmentColorScale, variableScale} from "../../common/colors";
import {toKeyCounts} from "../../common";


const BINDINGS = {
    databases: '='
};


function prepareStats(databases) {

    const environment = toKeyCounts(databases, d => d.environment);
    const vendor = toKeyCounts(databases, d => d.dbmsVendor);

    return {
        environment,
        vendor
    };
}


function controller($scope) {

    const vm = this;

    vm.pieConfig = {
        environment: {
            colorProvider: (d) => environmentColorScale(d.data.key)
        },
        vendor: {
            colorProvider: (d) => variableScale(d.data.key)
        }
    };

    const recalcPieData = (databases = []) => {
        if (databases.length > 0) {
            vm.pieData = prepareStats(databases);
        }
    };

    $scope.$watch(
        'ctrl.databases',
        (databases = []) => recalcPieData(databases)
    );

}

controller.$inject = [ '$scope' ];


export default () => ({
    restrict: 'E',
    replace: true,
    scope: {},
    template: require('./database-pies.html'),
    bindToController: BINDINGS,
    controllerAs: 'ctrl',
    controller
});

import {environmentColorScale, variableScale} from "../../common/colors";
import {toKeyCounts} from "../../common";
import {endOfLifeStatusNames} from "../../common/services/display_names";


const BINDINGS = {
    databases: '='
};


const PIE_SIZE = 70;


function prepareStats(databases) {

    const environment = toKeyCounts(databases, d => d.environment);
    const vendor = toKeyCounts(databases, d => d.dbmsVendor);
    const endOfLifeStatus = toKeyCounts(databases, d => d.endOfLifeStatus);

    return {
        environment,
        vendor,
        endOfLifeStatus
    };
}


function controller($scope) {

    const vm = this;

    vm.pieConfig = {
        environment: {
            size: PIE_SIZE,
            colorProvider: (d) => environmentColorScale(d.data.key)
        },
        vendor: {
            size: PIE_SIZE,
            colorProvider: (d) => variableScale(d.data.key)
        },
        endOfLifeStatus: {
            size: PIE_SIZE,
            colorProvider: (d) => variableScale(d.data.key),
            labelProvider: (d) => endOfLifeStatusNames[d.key] || "Unknown"
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

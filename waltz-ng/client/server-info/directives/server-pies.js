import {environmentColorScale, operatingSystemColorScale, variableScale} from "../../common/colors";
import {toKeyCounts} from "../../common";
import {endOfLifeStatusNames} from "../../common/services/display_names";


const BINDINGS = {
    servers : '='
};

const PIE_SIZE = 70;

const EOL_STATUS_CONFIG = {
    size: PIE_SIZE,
    colorProvider: (d) => variableScale(d.data.key),
    labelProvider: (d) => endOfLifeStatusNames[d.key] || "Unknown"
};

function controller($scope) {
    const vm = this;


    vm.pie = {
        env: {
            config: {
                size: PIE_SIZE,
                colorProvider: (d) => environmentColorScale(d.data.key)
            }
        },
        os: {
            config: {
                size: PIE_SIZE,
                colorProvider: (d) => operatingSystemColorScale(d.data.key)
            }
        },
        location: {
            config: {
                size: PIE_SIZE,
                colorProvider: (d) => variableScale(d.data.key)
            }
        },
        operatingSystemEndOfLifeStatus: {
            config: EOL_STATUS_CONFIG
        },
        hardwareEndOfLifeStatus: {
            config: EOL_STATUS_CONFIG
        }
    };

    function update(servers) {
        if (!servers) return;

        vm.pie.env.data = toKeyCounts(servers, d => d.environment);
        vm.pie.os.data = toKeyCounts(servers, d => d.operatingSystem);
        vm.pie.location.data = toKeyCounts(servers, d => d.location);
        vm.pie.operatingSystemEndOfLifeStatus.data = toKeyCounts(servers, d => d.operatingSystemEndOfLifeStatus);
        vm.pie.hardwareEndOfLifeStatus.data = toKeyCounts(servers, d => d.hardwareEndOfLifeStatus);
    }

    $scope.$watch('ctrl.servers', (servers) => update(servers), true);
}


controller.$inject = [ '$scope' ];

export default () => ({
    restrict: 'E',
    replace: true,
    scope: {},
    template: require('./server-pies.html'),
    bindToController: BINDINGS,
    controllerAs: 'ctrl',
    controller
});

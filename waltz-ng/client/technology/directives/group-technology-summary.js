import _ from "lodash";
import {environmentColorScale, operatingSystemColorScale, maturityColorScale, variableScale} from "../../common/colors";


const BINDINGS = {
    stats: '='
};

const PIE_SIZE = 70;

const PIE_CONFIG = {
    environment: {
        size: PIE_SIZE,
        colorProvider: (d) => environmentColorScale(d.data.key)
    },
    operatingSystem: {
        size: PIE_SIZE,
        colorProvider: (d) => operatingSystemColorScale(d.data.key)
    },
    location: {
        size: PIE_SIZE,
        colorProvider: (d) => variableScale(d.data.key)
    },
    vendor: {
        size: PIE_SIZE,
        colorProvider: (d) => variableScale(d.data.key)
    },
    maturity: {
        size: PIE_SIZE,
        colorProvider: (d) => maturityColorScale(d.data.key)
    }
};


const tallyToPieDatum = t => ({ key: t.id, count: t.count });
const prepareForPieChart = (tallies) => _.map(tallies, tallyToPieDatum);

function processServerStats(stats) {
    return {
        serverStats: {
            ...stats,
            environment: prepareForPieChart(stats.environmentCounts),
            operatingSystem: prepareForPieChart(stats.operatingSystemCounts),
            location: prepareForPieChart(stats.locationCounts)
        }
    };
}

function processDatabaseStats(stats) {
    return {
        databaseStats: {
            ...stats,
            environment: prepareForPieChart(stats.environmentCounts),
            vendor: prepareForPieChart(stats.vendorCounts)
        }
    };
}

function processSoftwareCatalogStats(stats) {
    return {
        softwareStats: {
            ...stats,
            vendor: prepareForPieChart(stats.vendorCounts),
            maturity: prepareForPieChart(stats.maturityCounts)
        }
    };
}


function controller($scope) {

    const vm = this;

    $scope.$watch(
        'ctrl.stats',
        stats => {
            if (! stats) return;
            Object.assign(vm, processServerStats(stats.serverStats));
            Object.assign(vm, processDatabaseStats(stats.databaseStats));
            Object.assign(vm, processSoftwareCatalogStats(stats.softwareStats));
        });


    vm.pieConfig = PIE_CONFIG;

}

controller.$inject = [ '$scope' ];


export default () => ({
    restrict: 'E',
    replace: true,
    scope: {},
    template: require('./group-technology-summary.html'),
    bindToController: BINDINGS,
    controllerAs: 'ctrl',
    controller
});

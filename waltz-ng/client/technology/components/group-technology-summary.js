import _ from "lodash";
import {environmentColorScale, operatingSystemColorScale, maturityColorScale, variableScale} from "../../common/colors";


const bindings = {
    stats: '<'
};

const initialState = {
    visibility: {
        servers: false,
        software: false,
        databases: false
    }
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


function calculateVisibility(stats) {
    return {
        servers: stats.serverStats.totalCount > 0,
        databases: stats.databaseStats.totalCount > 0,
        software: stats.softwareStats && _.keys(stats.softwareStats.vendorCounts).length > 0
    };
}

function controller() {

    const vm = _.defaultsDeep(this, initialState);

    vm.$onChanges = () => {
        if (! vm.stats) return;
        Object.assign(vm, processServerStats(vm.stats.serverStats));
        Object.assign(vm, processDatabaseStats(vm.stats.databaseStats));
        Object.assign(vm, processSoftwareCatalogStats(vm.stats.softwareStats));

        vm.visibility = calculateVisibility(vm.stats);
    };

    vm.pieConfig = PIE_CONFIG;
}


const component = {
    template: require('./group-technology-summary.html'),
    bindings,
    controller
};


export default component;

/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016  Khartec Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import _ from "lodash";
import {environmentColorScale, operatingSystemColorScale, maturityColorScale, variableScale} from "../../common/colors";
import {endOfLifeStatusNames} from "../../common/services/display_names";


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
    },
    endOfLifeStatus: {
        size: PIE_SIZE,
        colorProvider: (d) => variableScale(d.data.key),
        labelProvider: (d) => endOfLifeStatusNames[d.key] || "Unknown"
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
            location: prepareForPieChart(stats.locationCounts),
            operatingSystemEndOfLifeStatus: prepareForPieChart(stats.operatingSystemEndOfLifeStatusCounts),
            hardwareEndOfLifeStatus: prepareForPieChart(stats.hardwareEndOfLifeStatusCounts)
        }
    };
}

function processDatabaseStats(stats) {
    return {
        databaseStats: {
            ...stats,
            environment: prepareForPieChart(stats.environmentCounts),
            vendor: prepareForPieChart(stats.vendorCounts),
            endOfLifeStatus: prepareForPieChart(stats.endOfLifeStatusCounts)
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
        databases: stats.databaseStats && _.keys(stats.databaseStats.vendorCounts).length > 0,
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

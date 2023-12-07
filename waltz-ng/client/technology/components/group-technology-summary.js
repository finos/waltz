/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017, 2018, 2019 Waltz open source project
 * See README.md for more information
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific
 *
 */

import _ from "lodash";
import {
    environmentColorScale,
    maturityColorScale,
    operatingSystemColorScale,
    variableScale
} from "../../common/colors";
import { endOfLifeStatus } from "../../common/services/enums/end-of-life-status";
import template from "./group-technology-summary.html";
import {mkSelectionOptions} from "../../common/selector-utils";
import {CORE_API} from "../../common/services/core-api-utils";
import {mkLinkGridCell} from "../../common/grid-utils";


const bindings = {
    stats: "<",
    parentEntityRef: "<"
};

const initialState = {
    environmentDescription: "",
    licenseGridCols:  [
        mkLinkGridCell("Name", "name", "id", "main.licence.view"),
        {field: "externalId", displayName: "External Id"}
    ],
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
        colorProvider: (d) => environmentColorScale(d.key)
    },
    operatingSystem: {
        size: PIE_SIZE,
        colorProvider: (d) => operatingSystemColorScale(d.key)
    },
    location: {
        size: PIE_SIZE,
        colorProvider: (d) => variableScale(d.key)
    },
    vendor: {
        size: PIE_SIZE,
        colorProvider: (d) => variableScale(d.key)
    },
    maturity: {
        size: PIE_SIZE,
        colorProvider: (d) => maturityColorScale(d.key)
    },
    endOfLifeStatus: {
        size: PIE_SIZE,
        colorProvider: (d) => variableScale(d.key),
        labelProvider: (d) => endOfLifeStatus[d.key] ? endOfLifeStatus[d.key].name : "Unknown"
    }
};


const tallyToPieDatum = t => ({ key: t.id, count: t.count });
const prepareForPieChart = (tallies) => _.map(tallies, tallyToPieDatum);


function processServerStats(stats) {
    const serverStats = Object.assign({}, stats, {
        environment: prepareForPieChart(stats.environmentCounts),
        operatingSystem: prepareForPieChart(stats.operatingSystemCounts),
        location: prepareForPieChart(stats.locationCounts),
        operatingSystemEndOfLifeStatus: prepareForPieChart(stats.operatingSystemEndOfLifeStatusCounts),
        hardwareEndOfLifeStatus: prepareForPieChart(stats.hardwareEndOfLifeStatusCounts)
    });

    return {
        serverStats
    };
}

function processDatabaseStats(stats) {
    const databaseStats = Object.assign({}, stats, {
        environment: prepareForPieChart(stats.environmentCounts),
        vendor: prepareForPieChart(stats.vendorCounts),
        endOfLifeStatus: prepareForPieChart(stats.endOfLifeStatusCounts)
    });

    return {
        databaseStats
    };
}

function processSoftwareCatalogStats(stats) {
    const softwareStats = Object.assign({}, stats, {
        vendor: prepareForPieChart(stats.vendorCounts),
        maturity: prepareForPieChart(stats.maturityCounts)
    });
    return {
        softwareStats
    };
}


function calculateVisibility(stats) {
    return {
        servers: stats.serverStats.totalCount > 0,
        databases: stats.databaseStats && _.keys(stats.databaseStats.vendorCounts).length > 0,
        software: stats.softwareStats && _.keys(stats.softwareStats.vendorCounts).length > 0
    };
}


function controller(serviceBroker) {

    const vm = _.defaultsDeep(this, initialState);

    vm.$onInit = () => {
        serviceBroker
            .loadViewData(
                CORE_API.LicenceStore.findBySelector,
                [ mkSelectionOptions(vm.parentEntityRef) ])
            .then(r => {
                vm.licenses = r.data;
            });
    };

    vm.$onChanges = () => {
        if (! vm.stats) return;
        Object.assign(vm, processServerStats(vm.stats.serverStats));
        Object.assign(vm, processDatabaseStats(vm.stats.databaseStats));
        Object.assign(vm, processSoftwareCatalogStats(vm.stats.softwareStats));

        vm.visibility = calculateVisibility(vm.stats);

        const serverStats = vm.stats.serverStats;
        const totalServers = serverStats.totalCount;
        const totalEnvs = _.sumBy(serverStats.environmentCounts, d => d.count);


        vm.environmentDescription = totalEnvs > totalServers
            ? "Note: servers may support multiple environments"
            : "";

    };

    vm.pieConfig = PIE_CONFIG;
}


controller.$inject = [
    "ServiceBroker"
];

const component = {
    template,
    bindings,
    controller
};


export default {
    id: "waltzGroupTechnologySummary",
    component
};

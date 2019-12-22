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

import { environmentColorScale, operatingSystemColorScale, variableScale } from "../../../common/colors";
import { initialiseData, toKeyCounts } from "../../../common";
import { endOfLifeStatus } from "../../../common/services/enums/end-of-life-status";
import template from "./server-pies.html";


const bindings = {
    servers: "<",
    serverUsages: "<"
};


const PIE_SIZE = 70;

const EOL_STATUS_CONFIG = {
    size: PIE_SIZE,
    colorProvider: (d) => variableScale(d.key),
    labelProvider: (d) => endOfLifeStatus[d.key] ? endOfLifeStatus[d.key].name : "Unknown"
};

const multiEnvServerDescription = "Note: servers may support multiple environments";

const initialState = {
    environmentDescription: ""
};


function controller() {
    const vm = initialiseData(this, initialState);

    vm.pie = {
        env: {
            config: {
                size: PIE_SIZE,
                colorProvider: (d) => environmentColorScale(d.key)
            }
        },
        os: {
            config: {
                size: PIE_SIZE,
                colorProvider: (d) => operatingSystemColorScale(d.key)
            }
        },
        location: {
            config: {
                size: PIE_SIZE,
                colorProvider: (d) => variableScale(d.key)
            }
        },
        operatingSystemEndOfLifeStatus: {
            config: EOL_STATUS_CONFIG
        },
        hardwareEndOfLifeStatus: {
            config: EOL_STATUS_CONFIG
        }
    };


    function update(servers, serverUsages) {
        if (!servers) return;

        vm.pie.env.data = toKeyCounts(serverUsages, d => d.environment);
        vm.pie.os.data = toKeyCounts(servers, d => d.operatingSystem);
        vm.pie.location.data = toKeyCounts(servers, d => d.location);
        vm.pie.operatingSystemEndOfLifeStatus.data = toKeyCounts(servers, d => d.operatingSystemEndOfLifeStatus);
        vm.pie.hardwareEndOfLifeStatus.data = toKeyCounts(servers, d => d.hardwareEndOfLifeStatus);

        vm.environmentDescription = serverUsages.length > servers.length
            ? multiEnvServerDescription
            : "";
    }


    vm.$onChanges = () => {
        if(vm.servers) update(vm.servers, vm.serverUsages);
    };
}


controller.$inject = [ ];


const component = {
    bindings,
    template,
    controller
};


export default {
    id: "waltzServerPies",
    component
};

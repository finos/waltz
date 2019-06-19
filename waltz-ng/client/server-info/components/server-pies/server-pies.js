/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017  Waltz open source project
 * See README.md for more information
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import {environmentColorScale, operatingSystemColorScale, variableScale} from "../../../common/colors";
import {initialiseData, toKeyCounts} from "../../../common";
import {endOfLifeStatus} from "../../../common/services/enums/end-of-life-status";
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

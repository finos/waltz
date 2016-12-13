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

import {environmentColorScale, operatingSystemColorScale, variableScale} from "../../common/colors";
import {toKeyCounts} from "../../common";
import {endOfLifeStatusNames} from "../../common/services/display_names";


const bindings = {
    servers : '<'
};


const template = require('./server-pies.html');


const PIE_SIZE = 70;

const EOL_STATUS_CONFIG = {
    size: PIE_SIZE,
    colorProvider: (d) => variableScale(d.data.key),
    labelProvider: (d) => endOfLifeStatusNames[d.key] || "Unknown"
};


function controller() {
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


    vm.$onChanges = () => {
        if(vm.servers) update(vm.servers);
    };
}


controller.$inject = [ ];


const component = {
    bindings,
    template,
    controller
};


export default component;

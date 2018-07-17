/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017 Waltz open source project
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

import {environmentColorScale, variableScale} from "../../common/colors";
import {toKeyCounts, notEmpty} from "../../common";
import {endOfLifeStatus} from "../../common/services/enums/end-of-life-status";
import template from './database-pies.html';


const bindings = {
    databases: '<'
};




const PIE_SIZE = 70;


function prepareStats(databases = []) {

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
            labelProvider: (d) => endOfLifeStatus[d.key] ? endOfLifeStatus[d.key].name : "Unknown"
        }
    };

    const recalcPieData = (databases = []) => {
        if (notEmpty(databases)) {
            vm.pieData = prepareStats(databases);
        }
    };


    vm.$onChanges = () => {
        if(vm.databases) {
            recalcPieData(vm.databases);
        }
    };

}

controller.$inject = [ '$scope' ];


const component = {
    bindings,
    template,
    controller
};


export default component;

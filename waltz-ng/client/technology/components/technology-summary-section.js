/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016  Khartec Ltd.
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
import _ from "lodash";
import {initialiseData} from "../../common";


const bindings = {
    stats: '<',
    sourceDataRatings: '<'
};


const initialState = {
    stats: {

    },
    hasAnyData: false
};


const template = require('./technology-summary-section.html');


function controller() {
    const vm = initialiseData(this, initialState);

    vm.$onChanges = () => {
        if (vm.stats) {
            const hasServerStats = (vm.stats.serverStats && !_.isEmpty(vm.stats.serverStats.environmentCounts));
            const hasDbStats = (vm.stats.databaseStats && !_.isEmpty(vm.stats.databaseStats.environmentCounts));
            const hasSwStats = (vm.stats.softwareStats && !_.isEmpty(vm.stats.softwareStats.vendorCounts));

            vm.hasAnyData = hasServerStats || hasDbStats || hasSwStats;
        }
    };
}


controller.$inject = [];


const component = {
    bindings,
    template,
    controller
};


export default component;
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

import {ascending} from "d3-array";
import {nest} from "d3-collection";
import {initialiseData} from "../../../common/index";
import {CORE_API} from "../../../common/services/core-api-utils";

import template from './entity-statistic-section.html';

const bindings = {
    parentEntityRef: '<'
};


const initData = {
    entityStatisticsGrouped: []
};


function controller(serviceBroker) {
    const vm = initialiseData(this, initData);

    vm.$onChanges = (changes) => {
        if (vm.parentEntityRef) {
            serviceBroker
                .loadViewData(CORE_API.EntityStatisticStore.findStatsForEntity, [vm.parentEntityRef])
                .then(result => vm.entityStatisticsGrouped = nest()
                    .key(x => x.definition.category)
                    .sortKeys(ascending)
                    .key(x => x.definition.name)
                    .sortKeys(ascending)
                    .sortValues((a, b) => a.value.outcome < b.value.outcome)
                    .entries(result.data));
        }
    };
}


controller.$inject = [
    'ServiceBroker'
];


export default {
    component: {
        template,
        bindings,
        controller
    },
    id: 'waltzEntityStatisticSection'
};

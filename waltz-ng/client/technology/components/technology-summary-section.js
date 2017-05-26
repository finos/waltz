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
import {CORE_API} from '../../common/services/core-api-utils';


const bindings = {
    parentEntityRef: '<',
    scope: '@'
};


const initialState = {
    stats: {},
    hasAnyData: false
};


const template = require('./technology-summary-section.html');


function controller(serviceBroker) {
    const vm = initialiseData(this, initialState);

    vm.$onInit = () => {
        const selector = {
            entityReference: vm.parentEntityRef,
            scope: vm.scope
        };

        serviceBroker
            .loadAppData(CORE_API.SourceDataRatingStore.findAll)
            .then(r => vm.sourceDataRatings = r.data);

        serviceBroker
            .loadViewData(CORE_API.TechnologyStatisticsService.findBySelector, [selector])
            .then(r => {
                vm.stats = r.data;
                const {serverStats = {}, databaseStats = {}, softwareStats = {}} = vm.stats;

                const hasServerStats = (serverStats && !_.isEmpty(serverStats.environmentCounts));
                const hasDbStats = (databaseStats && !_.isEmpty(databaseStats.environmentCounts));
                const hasSwStats = (softwareStats && !_.isEmpty(softwareStats.vendorCounts));

                vm.hasAnyData = hasServerStats || hasDbStats || hasSwStats;
            });
    };
}


controller.$inject = [
    'ServiceBroker'
];


const component = {
    bindings,
    template,
    controller
};


export default component;
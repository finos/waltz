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

import {enrichServerStats} from "../../../server-info/services/server-utilities";
import {calcComplexitySummary} from "../../../complexity/services/complexity-utilities";
import template from './person-summary.html';
import {initialiseData} from "../../../common/index";
import {CORE_API} from "../../../common/services/core-api-utils";
import {mkSelectionOptions} from "../../../common/selector-utils";


const bindings = {
    parentEntityRef: '<',
    scope: '@?',
    person: '<',
    directs: '<',
    managers: '<',
    applications: '<',
    serverStats: '<',
    flows: '<'
};


const initialState = {
    scope: 'CHILDREN',
    organisationalUnit: null
};




function controller(serviceBroker) {
    const vm = initialiseData(this, initialState);

    vm.$onInit = () => {


    };

    vm.$onChanges = () => {
        if (vm.person && vm.person.organisationalUnitId) {
            serviceBroker
                .loadAppData(
                    CORE_API.OrgUnitStore.getById,
                    [ vm.person.organisationalUnitId ])
                .then(r => vm.organisationalUnit = Object.assign(
                    {},
                    r.data,
                    { kind: 'ORG_UNIT' }));
        }

        if (vm.parentEntityRef) {
            const selector = mkSelectionOptions(vm.parentEntityRef);

            serviceBroker
                .loadViewData(
                    CORE_API.ComplexityStore.findBySelector,
                    [ selector ])
                .then(r => vm.complexity = r.data);

            serviceBroker
                .loadViewData(
                    CORE_API.AssetCostStore.findTotalCostForAppSelector,
                    [ selector ])
                .then(r => vm.totalCost = r.data);

        }
        vm.complexitySummary = calcComplexitySummary(vm.complexity);
        vm.enrichedServerStats = enrichServerStats(vm.serverStats);
    }
}


controller.$inject = ['ServiceBroker'];


const component = {
    controller,
    template,
    bindings
};


export default {
    component,
    id: 'waltzPersonSummary'
};
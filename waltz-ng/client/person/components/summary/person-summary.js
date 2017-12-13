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

import {calcComplexitySummary} from "../../../complexity/services/complexity-utilities";
import template from './person-summary.html';
import {initialiseData} from "../../../common/index";
import {CORE_API} from "../../../common/services/core-api-utils";
import {mkSelectionOptions} from "../../../common/selector-utils";


const bindings = {
    parentEntityRef: '<'
};


const initialState = {
    scope: 'CHILDREN',
    organisationalUnit: null
};


function controller(serviceBroker) {
    const vm = initialiseData(this, initialState);

    vm.$onInit = () => {
        const selector = mkSelectionOptions(vm.parentEntityRef);

        serviceBroker
            .loadViewData(
                CORE_API.PersonStore.getById,
                [ vm.parentEntityRef.id ])
            .then(r => {
                vm.person = r.data;
                return serviceBroker
                    .loadAppData(
                        CORE_API.OrgUnitStore.getById,
                        [vm.person.organisationalUnitId]);
            })
            .then(r => vm.organisationalUnit = Object.assign(
                {},
                r.data,
                { kind: 'ORG_UNIT' }));

        serviceBroker
            .loadViewData(
                CORE_API.ApplicationStore.findBySelector,
                [ selector ])
            .then(r => vm.applications = r.data);

    };

    vm.$onChanges = () => {

        if (vm.parentEntityRef) {
            const selector = mkSelectionOptions(vm.parentEntityRef);

            serviceBroker
                .loadViewData(
                    CORE_API.ComplexityStore.findBySelector,
                    [ selector ])
                .then(r => {
                    vm.complexitySummary = calcComplexitySummary(r.data);
                });

            serviceBroker
                .loadViewData(
                    CORE_API.AssetCostStore.findTotalCostForAppSelector,
                    [ selector ])
                .then(r => vm.totalCost = r.data);

        }
    };
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
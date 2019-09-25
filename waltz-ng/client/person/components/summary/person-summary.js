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

import { calcComplexitySummary } from "../../../complexity/services/complexity-utilities";
import { initialiseData } from "../../../common/index";
import { CORE_API } from "../../../common/services/core-api-utils";
import { mkApplicationSelectionOptions } from "../../../common/selector-utils";
import { hierarchyQueryScope } from "../../../common/services/enums/hierarchy-query-scope";
import { entityLifecycleStatus } from "../../../common/services/enums/entity-lifecycle-status";

import template from "./person-summary.html";


const bindings = {
    filters: "<",
    parentEntityRef: "<"
};


const initialState = {
    organisationalUnit: null
};


function controller(serviceBroker) {
    const vm = initialiseData(this, initialState);

    const loadAll = () => {
        const selector = mkApplicationSelectionOptions(
            vm.parentEntityRef,
            hierarchyQueryScope.CHILDREN.key,
            [entityLifecycleStatus.ACTIVE.key],
            vm.filters);

        serviceBroker
            .loadViewData(
                CORE_API.PersonStore.getById,
                [ vm.parentEntityRef.id ])
            .then(r => {
                vm.person = r.data;

                if(vm.person.organisationalUnitId) {
                    return serviceBroker
                        .loadAppData(
                            CORE_API.OrgUnitStore.getById,
                            [vm.person.organisationalUnitId])
                        .then(r => vm.organisationalUnit = Object.assign(
                            {},
                            r.data,
                            {kind: "ORG_UNIT"}));
                }
            });

        serviceBroker
            .loadViewData(
                CORE_API.ApplicationStore.findBySelector,
                [ selector ])
            .then(r => vm.applications = r.data);


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
    };


    vm.$onInit = () => {
        loadAll();
    };

    vm.$onChanges = (changes) => {
        loadAll();
    };
}


controller.$inject = ["ServiceBroker"];


const component = {
    controller,
    template,
    bindings
};


export default {
    component,
    id: "waltzPersonSummary"
};
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

import _ from "lodash";
import {enrichServerStats} from "../../../server-info/services/server-utilities";
import {calcComplexitySummary} from "../../../complexity/services/complexity-utilities";
import {getParents, populateParents, switchToParentIds} from "../../../common/hierarchy-utils";
import {CORE_API} from "../../../common/services/core-api-utils";
import template from "./org-unit-overview.html";
import {initialiseData} from "../../../common/index";


const bindings = {
    parentEntityRef: "<",
};


const initialState = {
    visibility: {
        childDisplayMode: "LIST"
    }
};


function controller(serviceBroker) {
    const vm = initialiseData(this, initialState);

    vm.$onInit = () => {
        const selector = {
            entityReference: vm.parentEntityRef,
            scope: "CHILDREN"
        };

        serviceBroker
            .loadViewData(
                CORE_API.ApplicationStore.findBySelector,
                [ selector] )
            .then(r => vm.apps = r.data);

        serviceBroker
            .loadViewData(
                CORE_API.AssetCostStore.findTotalCostForAppSelector,
                [ selector ])
            .then(r => vm.totalCost = r.data);

        serviceBroker
            .loadViewData(
                CORE_API.ComplexityStore.findBySelector,
                [ selector ])
            .then(r => vm.complexitySummary = calcComplexitySummary(r.data));

        serviceBroker
            .loadViewData(
                CORE_API.LogicalFlowStore.calculateStats,
                [ selector ])
            .then(r => vm.flowStats = r.data);

        serviceBroker
            .loadViewData(
                CORE_API.TechnologyStatisticsService.findBySelector,
                [ selector ])
            .then(r => vm.enrichedServerStats = enrichServerStats(r.data.serverStats));

        serviceBroker
            .loadAppData(CORE_API.OrgUnitStore.findAll)
            .then(r => {
                const orgUnits = populateParents(r.data, true);
                vm.orgUnit = _.find(orgUnits, { id: vm.parentEntityRef.id });
                vm.parentOrgUnits = _.reverse(getParents(vm.orgUnit));
                vm.childOrgUnits = _.get(vm, "orgUnit.children", []);
                vm.descendantOrgUnitTree = switchToParentIds([ vm.orgUnit ]);
            });
    };
}


controller.$inject = ["ServiceBroker"];


const component = {
    bindings,
    template,
    controller
};


export default {
    component,
    id: "waltzOrgUnitOverview"
};

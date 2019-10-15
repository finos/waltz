/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017, 2018, 2019 Waltz open source project
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

import {entityLifecycleStatuses, resetData} from "../../common";
import {CORE_API} from "../../common/services/core-api-utils";
import {mkSelectionOptions} from "../../common/selector-utils";

import template from "./entity-statistic-summary-section.html";


const bindings = {
    filters: "<",
    parentEntityRef: "<"
};


const initData = {
    activeDefinition: null,
    activeSummary: null,
    definitions: [],
    loading: false,
    summaries: {}
};


function controller(serviceBroker) {
    const vm = resetData(this, initData);

    const loadStatTallies = (definition) => {
        vm.loading = true;
        const selectionOptions = mkSelectionOptions(
            vm.parentEntityRef,
            undefined,
            [
                entityLifecycleStatuses.ACTIVE,
                entityLifecycleStatuses.PENDING,
                entityLifecycleStatuses.REMOVED
            ],
            vm.filters);
        serviceBroker
            .loadViewData(CORE_API.EntityStatisticStore.findStatTallies, [[definition.id], selectionOptions])
            .then(result => {
                vm.loading = false;
                vm.summaries[definition.id] = result.data[0];
                vm.activeSummary = result.data[0];
            });
    };

    serviceBroker
        .loadAppData(CORE_API.EntityStatisticStore.findAllActiveDefinitions, [])
        .then(result => vm.definitions = result.data);

    vm.onDefinitionSelection = (d) => {
        vm.activeDefinition = d;
        if (vm.summaries[d.id]) {
            vm.loading = false;
            vm.activeSummary = vm.summaries[d.id];
        } else {
            loadStatTallies(d)
        }
    };


    vm.$onChanges = (changes) => {
        if(vm.activeDefinition && changes.filters) {
            loadStatTallies(vm.activeDefinition);
        }
    };
}


controller.$inject = [
    "ServiceBroker"
];


export default {
    component: {
        template,
        bindings,
        controller
    },
    id: "waltzEntityStatisticSummarySection"
};


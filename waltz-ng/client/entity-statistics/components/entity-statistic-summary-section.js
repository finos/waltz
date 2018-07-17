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

import {entityLifecycleStatuses, resetData} from "../../common";
import {CORE_API} from "../../common/services/core-api-utils";
import {mkSelectionOptions} from "../../common/selector-utils";
import template from './entity-statistic-summary-section.html';


const bindings = {
    parentEntityRef: '<'
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

    serviceBroker
        .loadAppData(CORE_API.EntityStatisticStore.findAllActiveDefinitions, [])
        .then(result => vm.definitions = result.data);

    vm.onDefinitionSelection = (d) => {
        vm.activeDefinition = d;
        if (vm.summaries[d.id]) {
            vm.loading = false;
            vm.activeSummary = vm.summaries[d.id];
        } else {
            vm.loading = true;
            const selectionOptions = mkSelectionOptions(
                vm.parentEntityRef,
                null,
                [
                    entityLifecycleStatuses.ACTIVE,
                    entityLifecycleStatuses.PENDING,
                    entityLifecycleStatuses.REMOVED
                ]);
            serviceBroker
                .loadViewData(CORE_API.EntityStatisticStore.findStatTallies, [[d.id], selectionOptions])
                .then(result => {
                    vm.loading = false;
                    vm.summaries[d.id] = result.data[0];
                    vm.activeSummary = result.data[0];
                });
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
    id: 'waltzEntityStatisticSummarySection'
};


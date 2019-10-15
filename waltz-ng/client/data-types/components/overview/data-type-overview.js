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

import {getParents} from "../../../common/hierarchy-utils";
import {CORE_API} from "../../../common/services/core-api-utils";
import {mkSelectionOptions} from "../../../common/selector-utils";
import {hierarchyQueryScope} from '../../../common/services/enums/hierarchy-query-scope';
import {lifecycleStatus} from '../../../common/services/enums/lifecycle-status';

import template from "./data-type-overview.html";


const bindings = {
    filters: "<",
    parentEntityRef: "<"
};


function controller(serviceBroker) {
    const vm = this;

    const loadAll = () => {
        const selector = mkSelectionOptions(
            vm.parentEntityRef,
            hierarchyQueryScope.CHILDREN.key,
            [lifecycleStatus.ACTIVE.key],
            vm.filters);

        serviceBroker
            .loadAppData(CORE_API.DataTypeStore.findAll)
            .then(r => {
                const dataTypes = r.data;
                const dataTypesById = _.keyBy(dataTypes, "id");
                const dataType = dataTypesById[vm.parentEntityRef.id];
                vm.dataType = dataType;
                vm.parents = getParents(dataType, n => dataTypesById[n.parentId]);
            });

        serviceBroker
            .loadViewData(
                CORE_API.ApplicationStore.findBySelector,
                [ selector ])
            .then(r => vm.apps = r.data);

        serviceBroker
            .loadViewData(
                CORE_API.DataTypeUsageStore.calculateStats,
                [ selector ])
            .then(r => vm.usageStats = r.data);
    };

    vm.$onInit = () => {
        loadAll();
    };


    vm.$onChanges = (changes) => {
        if(changes.filters) {
            loadAll();
        }
    };
}


controller.$inject = [
    "ServiceBroker"
];


const component = {
    bindings,
    template,
    controller
};


const id = "waltzDataTypeOverview";

export default {
    component,
    id
};

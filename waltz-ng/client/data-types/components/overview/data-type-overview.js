
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

import {getParents} from "../../../common/hierarchy-utils";
import {CORE_API} from "../../../common/services/core-api-utils";
import {mkSelectionOptions} from "../../../common/selector-utils";
import template from "./data-type-overview.html";


const bindings = {
    parentEntityRef: "<"
};


function controller(serviceBroker) {
    const vm = this;

    vm.$onInit = () => {
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
                [ mkSelectionOptions(vm.parentEntityRef) ])
            .then(r => vm.apps = r.data);

        serviceBroker
            .loadViewData(
                CORE_API.DataTypeUsageStore.calculateStats,
                [ mkSelectionOptions(vm.parentEntityRef) ])
            .then(r => vm.usageStats = r.data);
    }
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

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
import {buildHierarchies} from "../../../common/hierarchy-utils";
import {CORE_API} from "../../../common/services/core-api-utils";
import template from "./flow-filter-options.html";

const bindings = {
    onChange: "<",
    usedTypes: "<" // [ dataTypeId... ]
};


const initialState = {
    selectedType: "ALL",
    selectedScope: "ALL",
    visibility: {
        tree: false
    },
    onChange: () => console.log("No change handler registered for flow-filter-options-overlay::onChange")
};


function controller(serviceBroker) {
    const vm = _.defaults(this, initialState);

    vm.onShowAll= () => {
        vm.selectedType = "ALL";
        vm.visibility.tree = false;
        vm.notifyChanges();
    };

    vm.onShowTree= () => {
        vm.visibility.tree = true;
    };

    vm.onSelectType = (id) => {
        vm.selectedType = id;
        vm.notifyChanges()
    };

    vm.$onInit = () => {
        serviceBroker
            .loadAppData(CORE_API.DataTypeStore.findAll)
            .then(r => vm.allDataTypes = r.data);
    };

    vm.$onChanges = () => {
        vm.notifyChanges();
        const usedTypeIds = _.map(vm.usedTypes, "id");
        const enrichedDataTypes = _.map(vm.allDataTypes, dt => Object.assign({}, dt, { isUsed: _.includes(usedTypeIds, dt.id) }));
        vm.hierarchy = buildHierarchies(enrichedDataTypes, false);
    };

    vm.notifyChanges = () => {
        const options = {
            type: vm.selectedType || "ALL",
            scope: vm.selectedScope || "ALL"
        };
        vm.onChange(options);
    };
    // -- BOOT ---

    vm.notifyChanges();
}


controller.$inject = ["ServiceBroker"];


const component = {
    controller,
    bindings,
    template
};


export default component;
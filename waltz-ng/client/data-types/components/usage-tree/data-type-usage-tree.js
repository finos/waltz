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

import {initialiseData} from "../../../common/index";
import {CORE_API} from "../../../common/services/core-api-utils";
import {buildHierarchies} from "../../../common/hierarchy-utils";

import template from "./data-type-usage-tree.html";


const bindings = {
    used: "<"
};


const initialState = {};


function findParents(dtId, dataTypesById) {
    const branch = [ ];
    let cur = dataTypesById[dtId];
    while (cur) {
        branch.push(cur);
        cur = dataTypesById[cur.parentId];
    }
    return branch;
}


function enrichDataTypeWithExplicitFlag(dataType, explicitIds = []) {
    return Object.assign(
        {},
        dataType,
        { explicit: _.includes(explicitIds, dataType.id)});
}


function controller(serviceBroker) {
    const vm = initialiseData(this, initialState);

    vm.$onChanges = () => {
        if (! vm.used) return;
        serviceBroker
            .loadAppData(CORE_API.DataTypeStore.findAll)
            .then(r => {
                const dataTypesById = _.keyBy(r.data, "id");
                const explicitIds = _.map(vm.used, r => r.kind === "DATA_TYPE"
                        ? r.id
                        : r.dataTypeId);

                const requiredDataTypes = _
                    .chain(explicitIds)
                    .flatMap(dtId => findParents(dtId, dataTypesById))
                    .uniqBy("id")
                    .map(dataType => enrichDataTypeWithExplicitFlag(dataType, explicitIds))
                    .value();

                const hierarchy = buildHierarchies(requiredDataTypes, false);

                vm.expandedNodes = requiredDataTypes;
                vm.hierarchy = hierarchy;
            });
    };


    vm.treeOptions = {
        nodeChildren: "children",
        dirSelectable: true,
        equality: (a, b) => a && b && a.id === b.id
    };


}


controller.$inject = [
    "ServiceBroker"
];


const component = {
    template,
    bindings,
    controller
};


export default {
    id: "waltzDataTypeUsageTree",
    component
}
/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017, 2018, 2019 Waltz open source project
 * See README.md for more information
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific
 *
 */
import _ from "lodash";

import {initialiseData} from "../../../common/index";
import {CORE_API} from "../../../common/services/core-api-utils";
import {buildHierarchies} from "../../../common/hierarchy-utils";

import template from "./data-type-usage-tree.html";


const bindings = {
    used: "<?"
};


const initialState = {
    used: []
};


function findParents(dtId, dataTypesById) {
    const branch = [ ];
    let cur = dataTypesById[dtId];
    while (cur) {
        branch.push(cur);
        cur = dataTypesById[cur.parentId];
    }
    return branch;
}


function enrichDataTypeWithUsage(dataType, usedById = {}) {
    const usage = _.get(usedById, dataType.id, null);
    return {
        id: dataType.id,
        parentId: dataType.parentId,
        dataType,
        usage
    };
}


function controller(serviceBroker) {
    const vm = initialiseData(this, initialState);

    vm.$onChanges = () => {
        if (! vm.used) return;
        serviceBroker
            .loadAppData(CORE_API.DataTypeStore.findAll)
            .then(r => {
                const dataTypesById = _.keyBy(r.data, d => d.id);
                const usageById = _.keyBy(vm.used, d => d.dataTypeId);


                const requiredDataTypes = _
                    .chain(vm.used)
                    .flatMap(usage => findParents(usage.dataTypeId, dataTypesById))
                    .uniqBy(d => d.id)
                    .map(dataType => enrichDataTypeWithUsage(dataType, usageById))
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
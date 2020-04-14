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
import allUsageKinds from "../../usage-kinds";
import template from "./app-data-type-usage-list.html";
import {CORE_API} from "../../../common/services/core-api-utils";
import {buildHierarchies, mergeUpwards, reduceToSelectedNodesOnly} from "../../../common/hierarchy-utils";


const BINDINGS = {
    usages: "<"
};


const initialState = {
    consolidatedUsages:{},
    allUsageKinds,
    usages: [],
    treeOptions:  {
        nodeChildren: "children",
        equality: (a, b) => a && b && a.id === b.id
    }
};


function findUsage(usages = [], dataTypeId, usageKind) {
    return _.find(usages, { dataTypeId: Number(dataTypeId) , usage : { kind: usageKind }});
}


function mkUsageTree(dataTypes = [], usages = [], iconNameService) {

    const usagesByDataTypeId = _.groupBy(
        usages,
        u => u.dataTypeId);

    const merged = mergeUpwards(
        _.map(dataTypes, dt => Object
            .assign({}, dt, {directUsages: usagesByDataTypeId[dt.id]})),
        (p, c) => {
            const cUsages = usagesByDataTypeId[c.id] || [];
            const pUsages = usagesByDataTypeId[p.id] || [];
            return Object.assign(
                {},
                p,
                {
                    directUsages: pUsages,
                    usages: _
                        .chain([])
                        .concat(c.usages, cUsages, pUsages) // todo simplify
                        .compact()
                        .value()
                });
        });

    const mergedWithIcons = _.map(
        merged,
        d => {
            const icons = _
                .chain()
                .concat(d.usages || [], d.directUsages || [])
                .compact()
                .map(u => u.usage.kind)
                .uniq()
                .sort()
                .map(uk => iconNameService.lookup("usageKind", uk))
                .value();

            return Object.assign(d,  {icons});
        });

    const usedDTs = _
        .chain(usages)
        .map(u => u.dataTypeId)
        .uniq()
        .value();

    return buildHierarchies(
        reduceToSelectedNodesOnly(
            mergedWithIcons,
            usedDTs));
}


function controller(iconNameService, serviceBroker) {
    const vm = _.defaultsDeep(this, initialState);

    vm.$onInit = () => {
    };


    vm.$onChanges = (c) => {
        serviceBroker
            .loadAppData(CORE_API.DataTypeStore.findAll)
            .then(r => {
                vm.treeData = mkUsageTree(
                    r.data,
                    vm.usages,
                    iconNameService);
                console.log({ td: vm.treeData })
            });
    };


    vm.isSelected = (dataTypeId, usageKind) => {
        const foundUsage = findUsage(
            vm.usages,
            dataTypeId,
            usageKind);
        return foundUsage && foundUsage.usage.isSelected;
    };


    vm.hasDescription = (dataTypeId, usageKind) => {
        return vm.lookupDescription(dataTypeId, usageKind).length > 0;
    };


    vm.lookupDescription = (dataTypeId, usageKind) => {
        const foundUsage = findUsage(vm.usages, dataTypeId, usageKind);
        return _.get(
            foundUsage,
            ["usage", "description"],
            "");
    };
}


controller.$inject = [
    "IconNameService",
    "ServiceBroker"
];


const component = {
    template,
    controller,
    bindings: BINDINGS
};


export default {
    id: "waltzAppDataTypeUsageList",
    component
};


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
import allUsageKinds from "../usage-kinds";
import {notEmpty} from "../../common";
import template from "./app-data-type-usage-list.html";

const BINDINGS = {
    usages: "<"
};


const initialState = {
    consolidatedUsages:{},
    allUsageKinds,
    usages: []
};


function consolidateUsages(usages = []) {
    return _.chain(usages)
        .groupBy("dataTypeId")
        .mapValues(xs => _.chain(xs)
            .map("usage")
            .filter(u => u.isSelected || notEmpty(u.description))
            .value())
        .value();
}


function findUsage(usages = [], dataTypeId, usageKind) {
    return _.find(usages, { dataTypeId: Number(dataTypeId) , usage : { kind: usageKind }});
}


function controller($scope) {
    const vm = _.defaultsDeep(this, initialState);

    $scope.$watch(
        "ctrl.usages",
        (usages = []) => {
            vm.consolidatedUsages = consolidateUsages(usages);
        }
    );

    vm.isSelected = (dataTypeId, usageKind) => {
        const foundUsage = findUsage(vm.usages, dataTypeId, usageKind);
        return foundUsage && foundUsage.usage.isSelected;
    };

    vm.hasDescription = (dataTypeId, usageKind) => {
        const foundUsage = findUsage(vm.usages, dataTypeId, usageKind);
        return foundUsage && foundUsage.usage.description;
    };

    vm.lookupDescription = (dataTypeId, usageKind) => {
        const foundUsage = findUsage(vm.usages, dataTypeId, usageKind);
        return foundUsage
            ? foundUsage.usage.description
            : "";
    };
}


controller.$inject = ["$scope"];


const directive = {
    restrict: "E",
    replace: false,
    template,
    controller,
    controllerAs: "ctrl",
    scope: {},
    bindToController: BINDINGS
};


export default () => directive;


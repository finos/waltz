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
import allUsageKinds from "../usage-kinds";
import {notEmpty} from "../../common";
import template from './app-data-type-usage-list.html';

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
        .groupBy('dataTypeCode')
        .mapValues(xs => _.chain(xs)
            .map('usage')
            .filter(u => u.isSelected || notEmpty(u.description))
            .value())
        .value();
}


function findUsage(usages = [], dataTypeCode, usageKind) {
    return _.find(usages, { dataTypeCode , usage : { kind: usageKind }});
}

function controller($scope) {
    const vm = _.defaultsDeep(this, initialState);

    $scope.$watch(
        'ctrl.usages',
        (usages = []) => vm.consolidatedUsages = consolidateUsages(usages)
    );

    vm.isSelected = (dataTypeCode, usageKind) => {
        const foundUsage = findUsage(vm.usages, dataTypeCode, usageKind);
        return foundUsage && foundUsage.usage.isSelected;
    };

    vm.hasDescription = (dataTypeCode, usageKind) => {
        const foundUsage = findUsage(vm.usages, dataTypeCode, usageKind);
        return foundUsage && foundUsage.usage.description;
    };

    vm.lookupDescription = (dataTypeCode, usageKind) => {
        const foundUsage = findUsage(vm.usages, dataTypeCode, usageKind);
        return foundUsage
            ? foundUsage.usage.description
            : "";
    };

}


controller.$inject = ['$scope'];


const directive = {
    restrict: 'E',
    replace: false,
    template,
    controller,
    controllerAs: 'ctrl',
    scope: {},
    bindToController: BINDINGS
};


export default () => directive;


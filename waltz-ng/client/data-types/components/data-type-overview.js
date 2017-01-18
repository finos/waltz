
/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016  Khartec Ltd.
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

import {enrichServerStats} from "../../server-info/services/server-utilities";
import {calcComplexitySummary} from "../../complexity/services/complexity-utilities";
import {buildHierarchies, findNode, getParents} from "../../common/hierarchy-utils";

const bindings = {
    dataTypeId: '<',
    allDataTypes: '<',
    apps: '<',
    ratings: '<',
    serverStats: '<',
    complexity: '<',
    usageStats: '<'
};


const template = require('./data-type-overview.html');


function controller() {
    const vm = this;

    vm.$onChanges = () => {
        if(vm.allDataTypes) {
            const roots = buildHierarchies(vm.allDataTypes);
            const node = findNode(roots, vm.dataTypeId);
            vm.dataType = node;
            vm.parents = getParents(node);
        }

        if(vm.complexity) {
            vm.complexitySummary = calcComplexitySummary(vm.complexity);
        }

        if(vm.serverStats) {
            enrichServerStats(vm.serverStats);
        }
    };

}

controller.$inject = [];


const component = {
    bindings,
    template,
    controller
};


export default component;

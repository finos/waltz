
/*
 *  Waltz
 * Copyright (c) David Watkins. All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 *
 */
import {enrichServerStats} from "../../server-info/services/server-utilities";
import {calcComplexitySummary} from "../../complexity/services/complexity-utilities";
import {findNode, getParents} from "../../common";
import {prepareDataTypeTree} from "../utilities";


const bindings = {
    dataTypeId: '<',
    allDataTypes: '<',
    apps: '<',
    flows: '<',
    ratings: '<',
    serverStats: '<',
    complexity: '<',
    dataFlowTallies: '<',
    usageStats: '<'
};


const template = require('./data-type-overview.html');


function controller() {
    const vm = this;

    vm.$onChanges = () => {
        if(vm.allDataTypes) {
            const roots = prepareDataTypeTree(vm.allDataTypes, vm.dataFlowTallies);
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

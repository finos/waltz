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
import { buildHierarchies, initialiseData, switchToParentIds } from '../../common';


const bindings = {
    name: '@',
    definitions: '<',
    parentRef: '<'
};


const initData = {
    activeDefinition: null,
    activeSummary: null,
    definitions: [],
    loading: false,
    summaries: {}
};


function buildDefinitionTree(defns = []) {
    return switchToParentIds(buildHierarchies(defns));
}

const template = require('./entity-statistic-summary-section.html');


function controller(entityStatisticStore) {
    const vm = initialiseData(this, initData);

    vm.$onChanges = () => {
        vm.definitionTree = buildDefinitionTree(vm.definitions);
    };

    vm.onDefinitionSelection = (d) => {
        vm.activeDefinition = d;
        if (vm.summaries[d.id]) {
            vm.loading = false;
            vm.activeSummary = vm.summaries[d.id];
        } else {
            vm.loading = true;
            entityStatisticStore
                .findStatTallies([d.id], { entityReference: vm.parentRef, scope: 'CHILDREN' })
                .then(summaries => {
                    vm.loading = false;
                    vm.summaries[d.id] = summaries[0];
                    vm.activeSummary = summaries[0];
                });
        }
    };
}


controller.$inject = [
    'EntityStatisticStore'
];


const component = {
    template,
    controller,
    bindings
};


export default component;

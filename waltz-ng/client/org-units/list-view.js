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
import _ from "lodash";
import angular from "angular";
import {buildHierarchies, termSearch} from "../common";


const FIELDS_TO_SEARCH = ['name', 'description'];

function setupBlockProcessor($state) {
    return b => {
        b.block.onclick = () =>
            $state.go('main.org-units.unit', { id: b.value });

        angular
            .element(b.block)
            .addClass('clickable');
    };
}


function loadDiagrams(svgStore, vm, $state) {

    svgStore
        .findByKind('ORG_UNIT')
        .then(xs => vm.diagrams = xs);

    vm.blockProcessor = setupBlockProcessor($state);
}


function prepareOrgUnitTree(orgUnits, appTallies, endUserAppTallies) {
    const orgUnitsById = _.keyBy(orgUnits, 'id');

    const enrichWithDirectCounts = (tallies, keyName) => {
        _.each(tallies, t => {
            const ou = orgUnitsById[t.id];
            if (ou) ou[keyName] = t.count;
        });
    };

    enrichWithDirectCounts(appTallies, "appCount");
    enrichWithDirectCounts(endUserAppTallies, "endUserAppCount");

    const rootUnits = buildHierarchies(orgUnits);

    const summerFactory = (countKey, totalKey, childKey) => {
        const summer = (node) => {
            if (node == null) {
                return 0;
            }

            let temp = Number(node[countKey] || 0);
            let sum = _.sumBy(node.children, summer);

            if (node.children) {
                node[totalKey] = sum + temp;
                node[childKey] = sum;
            }

            return temp + sum;
        };
        return summer;
    };

    const appCountSummer = summerFactory("appCount",
        "totalAppCount",
        "childAppCount");
    const endUserAppCountSummer = summerFactory("endUserAppCount",
        "totalEndUserAppCount",
        "childEndUserAppCount");

    _.each(rootUnits, appCountSummer);
    _.each(rootUnits, endUserAppCountSummer);

    return rootUnits;
}


function controller(orgUnits,
                    appTallies,
                    endUserAppTallies,
                    svgStore,
                    $state) {

    const vm = this;

    loadDiagrams(svgStore, vm, $state);

    vm.filteredOrgUnits = [];
    vm.trees = prepareOrgUnitTree(orgUnits, appTallies, endUserAppTallies);
    vm.orgUnits = orgUnits;
    vm.nodeSelected = (node) => vm.selectedNode = node;

    vm.doSearch = (q) => {
        if (!q || q.length < 3) {
            vm.clearSearch();
        } else {
            vm.filteredOrgUnits = termSearch(orgUnits, q, FIELDS_TO_SEARCH);
        }
    };

    vm.clearSearch = (q) => vm.filteredOrgUnits = [];
}

controller.$inject = [
    'orgUnits',
    'appTallies',
    'endUserAppTallies',
    'SvgDiagramStore',
    '$state'
];


export default {
    template: require('./list-view.html'),
    controller,
    controllerAs: 'ctrl'
};

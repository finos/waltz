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
import {summerFactory} from "../common/tally-utils";


const FIELDS_TO_SEARCH = ['name', 'description'];

function setupBlockProcessor($state) {
    return b => {
        b.block.onclick = () =>
            $state.go('main.org-unit.view', { id: b.value });

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
                    sourceDataRatingStore,
                    staticPanelStore,
                    svgStore,
                    $state) {

    const vm = this;

    loadDiagrams(svgStore, vm, $state);

    staticPanelStore
        .findByGroup("HOME.ORG_UNIT")
        .then(panels => vm.panels = panels);

    sourceDataRatingStore
        .findAll()
        .then(sdrs => vm.sourceDataRatings = sdrs);

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
    'SourceDataRatingStore',
    'StaticPanelStore',
    'SvgDiagramStore',
    '$state'
];


export default {
    template: require('./list-view.html'),
    controller,
    controllerAs: 'ctrl'
};

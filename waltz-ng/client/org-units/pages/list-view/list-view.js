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
import angular from "angular";
import {termSearch} from "../../../common";
import {buildHierarchies} from "../../../common/hierarchy-utils";
import {buildPropertySummer} from "../../../common/tally-utils";
import template from './list-view.html';


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
        .findByGroup('ORG_UNIT')
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

    const appCountSummer = buildPropertySummer("appCount",
        "totalAppCount",
        "childAppCount");
    const endUserAppCountSummer = buildPropertySummer("endUserAppCount",
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
    template,
    controller,
    controllerAs: 'ctrl'
};

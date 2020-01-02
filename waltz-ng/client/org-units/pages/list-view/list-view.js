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

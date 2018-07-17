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
import {initialiseData} from "../common";
import {kindToViewState} from "../common/link-utils";
import template from './hierarchies-view.html';

const initialState = {
    combinedTallies: [],
    kinds: [
        'CHANGE_INITIATIVE',
        'DATA_TYPE',
        'ENTITY_STATISTIC',
        'ORG_UNIT',
        'MEASURABLE',
        'PERSON'
    ]
};


function combineTallies(entryCounts = [], rootCounts = []) {

    const rootTalliesKeyed = _.keyBy(rootCounts, 'id');

    return _.chain(entryCounts)
        .map(t => {
            const associatedRootCount = rootTalliesKeyed[t.id] || { count: 0 };
            return {
                id: t.id,
                hierarchyCount: t.count,
                rootCount: associatedRootCount.count
            };
        })
        .value();
}


function controller($q,
                    $state,
                    hierarchiesStore,
                    notification) {

    const vm = initialiseData(this, initialState);


    const loadTallies = () => {
        const promises = [
            hierarchiesStore.findTallies(),
            hierarchiesStore.findRootTallies()
        ];

        $q.all(promises)
            .then( ([tallies, rootTallies]) =>
                vm.combinedTallies = combineTallies(tallies, rootTallies));

    };

    vm.build = (kind) => {
        hierarchiesStore
            .buildForKind(kind)
            .then((count) => notification.success(`Hierarchy rebuilt for ${kind} with ${count} records`))
            .then(loadTallies);
    };


    vm.getRoots = (kind) => {
        hierarchiesStore
            .findRoots(kind)
            .then(roots => vm.roots = roots);
    };


    vm.goToRoot = (entityRef) => {
        if(entityRef.kind === 'ENTITY_STATISTIC') return;
        const stateName = kindToViewState(entityRef.kind);
        $state.go(stateName, { id: entityRef.id});
    };


    loadTallies();
}


controller.$inject = [
    '$q',
    '$state',
    'HierarchiesStore',
    'Notification',
];


export default {
    template,
    controller,
    controllerAs: 'ctrl',
    bindToController: true,
    scope: {}
};



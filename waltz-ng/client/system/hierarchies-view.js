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



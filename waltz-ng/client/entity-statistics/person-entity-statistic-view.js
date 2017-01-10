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

import _ from "lodash";
import {resetData} from "../common";
import {hasRelatedDefinitions, navigateToStatistic, updateUrlWithoutReload} from "./utilities";


const initData = {
    allDefinitions: [],
    statistic: {
        definition: null,
        summary: null,
        values: []
    },
    relatedDefinitions: null,
    bookmarks: [],
    summaries: [],
    directs: [],
    duration: 'MONTH',
    managers: [],
    peers: [],
    person: null,
    history: [],
    visibility: {
        related: false
    }
};


const template = require('./person-entity-statistic-view.html');



function mkHistory(history = [], current) {
    if (!current) return history;

    return _.concat([current], history);
}


function controller($q,
                    $state,
                    $stateParams,
                    applicationStore,
                    bookmarkStore,
                    entityStatisticStore,
                    personStore) {

    const vm = resetData(this, initData);
    const statId = $stateParams.statId;
    const personId = $stateParams.id;

    const personPromise = personStore
        .getById(personId);

    const statRef = {
        id: statId,
        kind: 'ENTITY_STATISTIC'
    };

    bookmarkStore
        .findByParent(statRef)
        .then(bs => vm.bookmarks = bs);

    const definitionPromise = entityStatisticStore
        .findRelatedStatDefinitions(statId)
        .then(ds => vm.relatedDefinitions = ds)
        .then(ds => vm.statistic.definition = ds.self)
        .then(() => vm.visibility.related = hasRelatedDefinitions(vm.relatedDefinitions));

    const allDefinitionsPromise = entityStatisticStore
        .findAllActiveDefinitions()
        .then(ds => vm.allDefinitions = ds);

    $q.all([personPromise, definitionPromise])
        .then(([person, definitions]) => vm.onSelectPerson(person))
        .then(allDefinitionsPromise);


    function resetValueData() {
        const clearData = resetData({}, initData);
        vm.statistic.summary = clearData.statistic.summary;
        vm.statistic.values = clearData.statistic.values;
        vm.summaries = clearData.summaries;
        vm.history = [];
    }

    function loadHistory() {
        const selector = {
            scope: 'CHILDREN',
            entityReference: vm.parentRef
        };

        entityStatisticStore
            .calculateHistoricStatTally(vm.statistic.definition, selector, vm.duration)
            .then(h => vm.history = mkHistory(h, vm.statistic.summary));
    }

    vm.onSelectPerson = (person) => {
        resetValueData();
        vm.person = person;

        const entityReference = {
            id: person.id,
            kind: 'PERSON'
        };
        vm.parentRef = entityReference;

        const selector = {
            scope: 'CHILDREN',
            entityReference
        };

        entityStatisticStore
            .calculateStatTally(vm.statistic.definition, selector)
            .then(summary => vm.statistic.summary = summary)
            .then(() => {
                const related = vm.relatedDefinitions.children;

                const relatedIds = _.chain(related)
                    .filter(s => s != null)
                    .map('id')
                    .value();

                return entityStatisticStore.findStatTallies(relatedIds, selector);
            })
            .then(summaries => vm.summaries = summaries);

        entityStatisticStore
            .findStatValuesByIdSelector(statId, selector)
            .then(stats => vm.statistic.values = stats);

        personStore
            .findDirects(person.employeeId)
            .then(directs => vm.directs = directs);

        personStore
            .findManagers(person.employeeId)
            .then(managers => vm.managers = managers);

        personStore
            .findDirects(person.managerEmployeeId)
            .then(peers => _.reject(peers, p => p.id === person.id))
            .then(peers => vm.peers = peers);

        applicationStore
            .findBySelector(selector)
            .then(apps => vm.applications = apps);

        loadHistory();

        updateUrlWithoutReload($state, person);
    };

    vm.onSelectDefinition = (node) => {
        navigateToStatistic($state, node.id, vm.parentRef);
    };


    vm.onChangeDuration = (d) => {
        vm.duration = d;
        loadHistory();
    }
}


controller.$inject = [
    '$q',
    '$state',
    '$stateParams',
    'ApplicationStore',
    'BookmarkStore',
    'EntityStatisticStore',
    'PersonStore'
];


const page = {
    controller,
    template,
    controllerAs: 'ctrl'
};


export default page;
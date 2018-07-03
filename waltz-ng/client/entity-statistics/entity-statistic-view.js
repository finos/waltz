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
import { entityLifecycleStatuses, resetData } from "../common";
import { kindToViewState } from "../common/link-utils";
import { mkSelectionOptions } from "../common/selector-utils";
import { hasRelatedDefinitions, navigateToStatistic, updateUrlWithoutReload } from "./utilities";
import { dynamicSections } from '../dynamic-section/dynamic-section-definitions';
import { CORE_API } from '../common/services/core-api-utils';


import template from './entity-statistic-view.html';


const initData = {
    allDefinitions: [],
    applications: [],
    orgUnits: [],
    statistic: {
        definition: null,
        summary: null,
        values: []
    },
    relatedDefinitions: null,
    summaries: [],
    navItems: [],
    selectedNavItem: null,
    parentStateRef: '.',
    history: [],
    duration: 'MONTH',
    visibility: {
        related: false
    },
    reloading: false,
    bookmarkSection: dynamicSections.bookmarksSection
};


function mkHistory(history = [], current) {
    if (!current) return history;

    return _.concat([current], history);
}


function mkStatisticSelector(entityRef) {
    const selector = mkSelectionOptions(entityRef);
    selector.entityLifecycleStatuses = [
        entityLifecycleStatuses.ACTIVE,
        entityLifecycleStatuses.PENDING,
        entityLifecycleStatuses.REMOVED
    ];

    return selector;
}


function controller($q,
                    $state,
                    $stateParams,
                    entityStatisticUtilities,
                    serviceBroker) {
    const vm = resetData(this, initData);

    const statId = $stateParams.statId;
    const entityKind = $stateParams.kind;
    const entityId = $stateParams.id;

    vm.statRef = {
        id: statId,
        kind: 'ENTITY_STATISTIC'
    };

    const definitionPromise = serviceBroker
        .loadViewData(CORE_API.EntityStatisticStore.findRelatedStatDefinitions, [statId])
        .then(r => r.data)
        .then(ds => vm.relatedDefinitions = ds)
        .then(ds => vm.statistic.definition = ds.self)
        .then(() => vm.statRef = Object.assign({}, vm.statRef, { name: vm.statistic.definition.name }))
        .then(() => vm.visibility.related = hasRelatedDefinitions(vm.relatedDefinitions));

    const navItemPromise = entityStatisticUtilities
        .findAllForKind(entityKind, entityId)
        .then(xs => vm.navItems = xs);

    const allDefinitionsPromise = serviceBroker
        .loadViewData(CORE_API.EntityStatisticStore.findAllActiveDefinitions, [])
        .then(r => vm.allDefinitions = r.data);

    const orgUnitsPromise = serviceBroker
        .loadAppData(CORE_API.OrgUnitStore.findAll, [])
        .then(r => vm.orgUnits = r.data);

    $q.all([navItemPromise, definitionPromise])
        .then(() => /* boot */ vm.onSelectNavItem(_.find(vm.navItems, { id: entityId })))
        .then(allDefinitionsPromise)
        .then(orgUnitsPromise);

    function resetValueData() {
        const clearData = resetData({}, initData);
        vm.statistic.summary = clearData.statistic.summary;
        vm.statistic.values = clearData.statistic.values;
        vm.summaries = clearData.summaries;
        vm.history = [];
    }

    function loadHistory() {
        const selector = mkStatisticSelector(vm.parentRef);

        serviceBroker
            .loadViewData(
                CORE_API.EntityStatisticStore.calculateHistoricStatTally,
                [vm.statistic.definition, selector, vm.duration])
            .then(r => vm.history = mkHistory(r.data, vm.statistic.summary));
    }


    vm.onSelectNavItem = (navItem) => {
        vm.reloading = true;

        resetValueData();

        vm.selectedNavItem = navItem;

        const entityReference = {
            id: navItem.id,
            kind: entityKind
        };
        vm.parentRef = entityReference;


        const selector = mkStatisticSelector(entityReference);

        serviceBroker
            .loadViewData(
                CORE_API.EntityStatisticStore.calculateStatTally,
                [vm.statistic.definition, selector])
            .then(r => {
                vm.statistic.summary = r.data;
                vm.reloading = false;
            })
            .then(() => vm.history = mkHistory(vm.history, vm.statistic.summary))
            .then(() => {
                const related = vm.relatedDefinitions.children;

                const relatedIds = _.chain(related)
                    .filter(s => s !== null)
                    .map('id')
                    .value();

                return serviceBroker
                    .loadViewData(
                        CORE_API.EntityStatisticStore.findStatTallies,
                        [relatedIds, selector])
                    .then(r => r.data)
            })
            .then(summaries => vm.summaries = summaries);

        serviceBroker
            .loadViewData(
                CORE_API.EntityStatisticStore.findStatValuesByIdSelector,
                [statId, selector])
            .then(r => vm.statistic.values = r.data);

        serviceBroker
            .loadViewData(
                CORE_API.EntityStatisticStore.findStatAppsByIdSelector,
                [statId, selector])
            .then(r => vm.applications = r.data);

        loadHistory();

        updateUrlWithoutReload($state, navItem);
    };

    vm.onSelectDefinition = (node) => {
        navigateToStatistic($state, node.id, vm.parentRef);
    };

    vm.goToParent = () => {
        const stateName = kindToViewState(entityKind);
        const navId = vm.selectedNavItem.id;
        $state.go(stateName, { id: navId });
    };

    vm.onChangeDuration = (d) => {
        vm.duration = d;
        loadHistory();
    };
}


controller.$inject = [
    '$q',
    '$state',
    '$stateParams',
    'EntityStatisticUtilities',
    'ServiceBroker'
];


const view = {
    template,
    controller,
    controllerAs: 'ctrl',
    bindToController: true,
    scope: {}
};


export default view;
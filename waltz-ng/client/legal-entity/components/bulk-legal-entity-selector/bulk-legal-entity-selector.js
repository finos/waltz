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
import {CORE_API} from "../../../common/services/core-api-utils";
import {initialiseData} from "../../../common";
import {invokeFunction} from "../../../common/index";
import {sameRef, toEntityRef} from '../../../common/entity-utils';

import template from "./bulk-legal-entity-selector.html";
import {action} from "../../../common/services/enums/action";


const bindings = {
    existingRefs: '<',
    onSave: '<',
    itemId: "<?", // ctx
    required: "<?"
};

const initialState = {
    bulkEntriesString: '',
    existingRefs: [],
    searchResults: [],
    filteredSearchResults: [],
    removedResults: [],
    searchSummary: {},
    showNotFoundOnly: false,
    visibility: {
        editor: true,
        loading: false
    },
    required: false,
    isDirty: false,


    onSave: (entityRefs) => console.log('default onSave handler for bulk-legal-entity-selector: ', entityRefs)
};


function mkSummary(searchResults = []) {
    const basicStats = {
        total: searchResults.length,
        removedEntityCount: _
            .chain(searchResults)
            .filter(r => _.get(r, ["entityRef", "entityLifecycleStatus"]) === 'REMOVED')
            .size()
            .value()
    };

    const countsByFoundStatus = _.countBy(
        searchResults,
        r => r.entityRef == null
            ? "notFound"
            : "found");

    return Object.assign(
        {},
        basicStats,
        countsByFoundStatus);
}


function determineAction(existingRef, searchedRef) {
    if (!searchedRef) return;

    if (!existingRef) {
        return 'ADD';
    } else if (sameRef(existingRef, searchedRef)) {
        return 'NO_CHANGE';
    }
}


function findMatched(legalEntities = [], identifiers = [], existingRefs = []) {
    const lesByExternalId = _.keyBy(legalEntities, 'externalId');
    const existingRefsById = _.keyBy(existingRefs, 'id');

    return _
        .chain(identifiers)
        .map(identifier => {
            const le = lesByExternalId[identifier];
            const entityRef = le ? toEntityRef(le, 'LEGAL_ENTITY') : null;

            return {
                identifier,
                entityRef,
                action: entityRef ? determineAction(existingRefsById[entityRef.id], entityRef) : null
            };
        })
        .value();
}


function controller(serviceBroker) {
    const vm = initialiseData(this, initialState);

    vm.$onChanges = (changes) => {
        if (vm.existingRefs) {
            vm.bulkEntriesString = _.chain(vm.existingRefs)
                .map('externalId')
                .join('\n')
                .value();

            vm.resolve();
        }
    };

    const searchRefs = (identifiers) => {
        return serviceBroker
            .loadViewData(CORE_API.LegalEntityStore.findAll)
            .then(r => {
                const allLegalEntities = r.data;
                return findMatched(allLegalEntities, identifiers, vm.existingRefs);
            });
    };


    const filterResults = () => {
        return _.filter(vm.searchResults, r => vm.showNotFoundOnly ? r.entityRef == null : true);
    };


    // -- INTERACT --
    vm.resolve = () => {
        vm.searchResults = [];
        vm.visibility.loading = true;
        vm.visibility.editor = false;
        vm.showNotFoundOnly = false;

        const separatorsRegExp = /,|;|\n|\|/;
        const identifiers = _.map(_.split(vm.bulkEntriesString, separatorsRegExp), s => _.trim(s));

        return searchRefs(identifiers)
            .then(results => {
                vm.searchResults = results;
                vm.filteredSearchResults = filterResults();
                vm.visibility.loading = false;
                vm.searchSummary = mkSummary(vm.searchResults);

                const resultsById = _.keyBy(results, "entityRef.id");
                vm.removedResults = _.chain(vm.existingRefs)
                    .filter(r => !resultsById[r.id])
                    .map(entityRef => ({entityRef, action: "REMOVE"}))
                    .value();

                vm.isDirty = vm.removedResults.length > 0 || !_.every(vm.searchResults, a => a.action == action.NO_CHANGE.key)
            });
    };

    vm.save = () => {
        if (!vm.searchSummary.notFound || confirm(`There are {${vm.searchSummary.notFound}} unresolved legal entities, do you want to proceed?`)){
            const entityRefs = _.map(vm.searchResults, k => ({
                id: k.entityRef.id,
                kind: k.entityRef.kind,
                name: `${k.entityRef.name} (${k.entityRef.externalId}:LEGAL_ENTITY/${k.entityRef.id})`,
                externalId: k.entityRef.externalId,
                entityLifecycleStatus: k.entityRef.entityLifecycleStatus
            }));
            invokeFunction(vm.onSave, vm.itemId, entityRefs);
        }
    };

    vm.toggleNotFound = () => {
        vm.showNotFoundOnly = !vm.showNotFoundOnly;
        vm.filteredSearchResults = filterResults();
    };
}


controller.$inject = [
    'ServiceBroker'
];


const component = {
    template,
    bindings,
    controller
};


export default {
    component,
    id: 'waltzBulkLegalEntitySelector'
};

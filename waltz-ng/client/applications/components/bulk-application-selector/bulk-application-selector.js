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

import template from "./bulk-application-selector.html";


const bindings = {
    existingRefs: '<',
    onSave: '<'
};

const MODES = {
    ADD: 'ADD',
    REPLACE: 'REPLACE'
};

const initialState = {
    bulkEntriesString: '',
    existingRefs: [],
    searchResults: [],
    filteredSearchResults: [],
    mode: 'ADD', //ADD | REPLACE
    removedResults: [],
    searchSummary: {},
    showNotFoundOnly: false,
    visibility: {
        editor: true,
        loading: false
    },

    onSave: (entityRefs) => console.log('default onSave handler for bulk-application-selector: ', entityRefs)
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


function findMatchedApps(apps = [], identifiers = [], existingRefs = []) {
    const appsByAssetCode = _.keyBy(apps, 'assetCode');
    const existingRefsById = _.keyBy(existingRefs, 'id');

    return _
        .chain(identifiers)
        .map(identifier => {
            const app = appsByAssetCode[identifier];
            const entityRef = app ? toEntityRef(app) : null;

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

    const searchRefs = (identifiers) => {
        return serviceBroker
            .loadViewData(CORE_API.ApplicationStore.findAll)
            .then(r => {
                const allApps = r.data;
                return findMatchedApps(allApps, identifiers, vm.existingRefs);
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
            });
    };

    vm.save = () => {
        vm.selectionResults = _.filter(vm.searchResults, r => r.action !== "NO_CHANGE");
        if(vm.mode === MODES.REPLACE) {
            vm.selectionResults = _.concat(vm.selectionResults, vm.removedResults);
        }

        if (!vm.searchSummary.notFound || confirm(`There are {${vm.searchSummary.notFound}} unresolved applications, do you want to proceed?`)){
            invokeFunction(vm.onSave, vm.selectionResults);
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
    id: 'waltzBulkApplicationSelector'
};

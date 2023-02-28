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
import {sameRef, toEntityRef} from "../../../common/entity-utils";

import template from "./bulk-assessment-rating-selector.html";


const bindings = {
    definition: "<",
    existingRefs: "<",
    ratingItems: "<",
    onSave: "<"
};

const MODES = {
    ADD: "ADD",
    REPLACE: "REPLACE"
};

const initialState = {
    bulkEntriesString: "",
    existingRefs: [],
    searchResults: [],
    filteredSearchResults: [],
    mode: "ADD", //ADD | REPLACE
    removedResults: [],
    searchSummary: {},
    showNotFoundOnly: false,
    visibility: {
        editor: true,
        loading: false
    },

    onSave: (entityRefs) => console.log("default onSave handler for bulk-assessment-rating-selector: ", entityRefs)
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
        r => r.entityRef == null || r.rating === undefined
                    ? "notFound"
                    : "found");

    return Object.assign(
        {},
        basicStats,
        countsByFoundStatus);
}


function determineAction(definition, existingRefs = [], searchedRef) {
    if (!searchedRef.entityRef || !searchedRef.rating) return;

    const isSingleValued = definition.cardinality === "ZERO_ONE";

    const entityHasRating = _.some(existingRefs, d => sameRef(d.entityRef, searchedRef.entityRef));
    const existingRating = _.find(existingRefs, d => sameRef(d.entityRef, searchedRef.entityRef) && d.rating.id === searchedRef.rating.id);

    if (existingRating && searchedRef.comment === existingRating.comment) {
        return "NO_CHANGE";
    } else if (existingRating) {
        return "UPDATE";
    } else if (entityHasRating && isSingleValued) {
        return "UPDATE";
    } else {
        return "ADD"; //multi-valued assessments will by default add a new one
    }
}


function findMatchedApps(definition, apps = [], identifiers = [], existingRefs = [], ratingItems = []) {

    const appsByAssetCode = _.keyBy(apps, "assetCode");
    const existingRefsById = _.groupBy(existingRefs, "entityRef.id");

    return _.chain(identifiers)
        .map(identifier => {
            const app = appsByAssetCode[identifier.appIdentifier];
            const selectedRating = _.find(ratingItems,
                item => item.name === identifier.rating
                    || item.rating === _.toUpper(identifier.rating));
            const entityRef = app ? toEntityRef(app, "APPLICATION") : null;
            const searchEntity = Object.assign({}, {
                entityRef: entityRef,
                rating: selectedRating,
                comment: identifier.comment,
                identifier: identifier.appIdentifier
            });
            const action = entityRef ? determineAction(definition, existingRefsById[entityRef.id], searchEntity) : null;
            return _.assign(searchEntity , {action: action});
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
                return findMatchedApps(vm.definition, allApps, identifiers, vm.existingRefs, vm.ratingItems);
            });
    };


    const filterResults = () => {
        return _.filter(vm.searchResults, r => vm.showNotFoundOnly ? r.entityRef == null : true);
    };

    const createRatings = (lines) => {
        const columnSeparatorsRegExp = /,|;|\t/;

        return _.map(lines, line => {
            const columns = _.split(line, columnSeparatorsRegExp);
            return Object.assign({}, {
                appIdentifier: _.trim(columns[0]),
                rating: _.trim(columns[1]),
                comment: _.trim(columns.length === 2 ? columns[2] : _.slice(columns, 2, columns.length).join(","))
            })});
    };


    // -- INTERACT --
    vm.resolve = () => {
        vm.searchResults = [];
        vm.visibility.loading = true;
        vm.visibility.editor = false;
        vm.showNotFoundOnly = false;

        const separatorsRegExp = /\n|\|/;
        const lines = _.split(vm.bulkEntriesString, separatorsRegExp);
        const identifiers = createRatings(lines);

        return searchRefs(identifiers)
            .then(results => {
                vm.searchResults = results;
                vm.filteredSearchResults = filterResults();
                vm.visibility.loading = false;

                vm.searchSummary = mkSummary(vm.searchResults);
                const isSingleValued = vm.definition.cardinality === "ZERO_ONE";

                vm.removedResults = _.chain(vm.existingRefs)
                    .filter(r => {
                        const toBeUpdated = isSingleValued && _.some(vm.searchResults, d => sameRef(r.entityRef, d.entityRef));
                        const noChange = _.some(vm.searchResults, d => sameRef(r.entityRef, d.entityRef) && d.rating.id === r.rating.id);
                        return !(toBeUpdated || noChange);
                    })
                    .map(r => ({entityRef: r.entityRef, action: "REMOVE", rating: r.rating}))
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

    vm.isUnidentified = (result) => {
        return !result.entityRef || !result.rating;
    }
}


controller.$inject = [
    "ServiceBroker"
];


const component = {
    template,
    bindings,
    controller
};


export default {
    component,
    id: "waltzBulkAssessmentRatingSelector"
};

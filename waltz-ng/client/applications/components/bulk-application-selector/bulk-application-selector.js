/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2017  Khartec Ltd.
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
import { CORE_API } from "../../../common/services/core-api-utils";
import { initialiseData } from "../../../common";
import { invokeFunction } from "../../../common/index";

import template from "./bulk-application-selector.html";


const bindings = {
    onSave: '<'
};


const initialState = {
    bulkEntriesString: '',
    searchResults: [],
    filteredSearchResults: [],
    searchSummary: {},
    showNotFoundOnly: false,
    visibility: {
        editor: true,
        loading: false
    },

    onSave: (entityRefs) => console.log('default onSave handler for bulk-application-selector: ', entityRefs)
};


function mkSummary(searchResults = []) {
    const summary = _
        .chain(searchResults)
        .groupBy(r => r.entityRef == null ? 'notFound' : 'found')
        .reduce((r, v, k) => {
            r[k] = v.length;
            return r;
        }, {})
        .value();
    summary.total = searchResults.length;
    return summary;
}


function findMatchedApps(apps = [], identifiers = []) {
    const appsByAssetCode = _.keyBy(apps, 'assetCode');

    return _.chain(identifiers)
        .map(identifier => ({
            identifier,
            entityRef: mkRef(appsByAssetCode[identifier])
        }))
        .value();
}


function mkRef(app) {
    if(!app) {
        return null;
    }

    return {
        kind: 'APPLICATION',
        id: app.id,
        name: app.name,
        description: app.description
    };
}


function controller($q, serviceBroker) {
    const vm = initialiseData(this, initialState);

    vm.options = {
        entityKinds: ['APPLICATION'],
        limit: 1
    };


    const searchRefs = (identifiers) => {
        return serviceBroker
            .loadViewData(CORE_API.ApplicationStore.findAll)
            .then(r => {
                const allApps = r.data;
                return findMatchedApps(allApps, identifiers);
            });
    };


    const filterResults = () => {
        vm.filteredSearchResults = _.filter(vm.searchResults, r => vm.showNotFoundOnly ? r.entityRef == null : true);
    };


    // -- INTERACT --
    vm.resolve = () => {
        vm.searchResults = [];
        vm.visibility.loading = true;
        vm.visibility.editor = false;
        vm.showNotFoundOnly = false;

        const separatorsRegExp = /,|;|\n|\|/;
        const identifiers = _.split(vm.bulkEntriesString, separatorsRegExp);

        return searchRefs(identifiers)
            .then(results => {
                vm.searchResults = results;
                filterResults();
                vm.visibility.loading = false;
                vm.searchSummary = mkSummary(vm.searchResults);
            });
    };

    vm.save = () => invokeFunction(vm.onSave, vm.searchResults);

    vm.toggleNotFound = () => {
        vm.showNotFoundOnly = !vm.showNotFoundOnly;
        filterResults();
    }
}


controller.$inject = [
    '$q',
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

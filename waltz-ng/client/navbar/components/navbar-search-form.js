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
import {initialiseData} from "../../common";


const bindings = {
};


const initialState = {
    query: '',
    searchResults: {
        show: false,
        apps: [],
        people: [],
        capabilities: [],
        orgUnits: []
    }
};


const template = require('./navbar-search-form.html');


function controller($timeout,
                    actorStore,
                    applicationStore,
                    measurableStore,
                    personStore,
                    physicalFlowStore,
                    orgUnitStore) {
    const searchResults = {
        show: false
    };

    const vm = initialiseData(this, initialState);

    // helper fn, to reduce boilerplate
    const handleSearch = (query, store, resultKey) => {
        return store
            .search(query)
            .then(r => searchResults[resultKey] = r || []);
    };

    const doSearch = (query) => {
        if (_.isEmpty(query)) {
            searchResults.show = false;
        } else {
            searchResults.show = true;
            handleSearch(query, applicationStore, 'apps');
            handleSearch(query, personStore, 'people');
            handleSearch(query, measurableStore, 'measurables');
            handleSearch(query, orgUnitStore, 'orgUnits');
            handleSearch(query, actorStore, 'actors');

            physicalFlowStore
                .searchReports(query)
                .then(xs => searchResults.reports = xs || []);
        }
    };

    const dismissResults = (e) => $timeout(
        () => searchResults.show = false,
        200);

    vm.searchResults = searchResults;
    vm.doSearch = () => doSearch(vm.query);
    vm.showSearch = () => searchResults.show;
    vm.dismissResults = dismissResults;
}


controller.$inject = [
    '$timeout',
    'ActorStore',
    'ApplicationStore',
    'MeasurableStore',
    'PersonStore',
    'PhysicalFlowStore',
    'OrgUnitStore'
];


const component = {
    bindings,
    controller,
    template
};


export default component;
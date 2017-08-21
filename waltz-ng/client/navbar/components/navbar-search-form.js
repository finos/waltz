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
import {CORE_API} from "../../common/services/core-api-utils";
import template from './navbar-search-form.html';

const bindings = {
};


const initialState = {
    query: '',
    searchResults: {
        show: false,
        apps: [],
        people: [],
        capabilities: [],
        changeInitiatives: [],
        orgUnits: []
    }
};




function controller($timeout,
                    serviceBroker,
                    displayNameService) {

    const vm = initialiseData(this, initialState);

    const searchAppGroups = (q) => {
        let groups = [];

        const prepareResults = (gs, q) => {
            return _
                .chain(gs)
                .filter(g => _.includes(_.lowerCase(g.name), q))
                .map(g => ({
                    kind: 'APP_GROUP',
                    id: g.id,
                    name: g.name,
                    description: g.description,
                    qualifier: g.kind === 'PUBLIC' ? 'Public group' : 'Private Group'
                }))
                .value();
        };

        serviceBroker
            .loadViewData(CORE_API.AppGroupStore.findPublicGroups)
            .then(r => groups = _.union(groups, r.data))
            .then(() => serviceBroker.loadViewData(CORE_API.AppGroupStore.findPrivateGroups))
            .then(r => groups = _.union(groups, r.data))
            .then(r => vm.searchResults.APP_GROUP = prepareResults(groups, q));

    };

    // helper fn, to reduce boilerplate
    const handleSearch = (query, searchAPI, entityKind) => {
        const transformResult = r => {
            let qualifier = null;

            switch (entityKind) {
                case 'APPLICATION':
                    qualifier = r.assetCode;
                    break;
                case 'MEASURABLE':
                    qualifier = displayNameService.lookup('measurableCategory', r.categoryId)
                    break;
                default:
                    qualifier = r.externalId || '';
                    break;
            };

            return {
                id: r.id,
                kind: entityKind,
                name: r.name || r.displayName,
                qualifier,
                description: r.description
            };
        };

        return serviceBroker
            .loadViewData(searchAPI, [ query ])
            .then(r => vm.searchResults[entityKind] = _.map(r.data, transformResult));
    };

    const doSearch = (query) => {
        if (_.isEmpty(query)) {
            vm.searchResults.show = false;
        } else {
            vm.searchResults.show = true;
            handleSearch(query, CORE_API.ApplicationStore.search, 'APPLICATION');
            handleSearch(query, CORE_API.ChangeInitiativeStore.search, 'CHANGE_INITIATIVE');
            handleSearch(query, CORE_API.PersonStore.search, 'PERSON');
            handleSearch(query, CORE_API.MeasurableStore.search, 'MEASURABLE');
            handleSearch(query, CORE_API.OrgUnitStore.search, 'ORG_UNIT');
            handleSearch(query, CORE_API.ActorStore.search, 'ACTOR');
            handleSearch(query, CORE_API.PhysicalSpecificationStore.search, 'PHYSICAL_SPECIFICATION');

            searchAppGroups(query);
        }
    };

    const dismissResults = (e) => $timeout(
        () => vm.searchResults.show = false,
        200);

    vm.doSearch = () => doSearch(vm.query);
    vm.showSearch = () => vm.searchResults.show;
    vm.dismissResults = dismissResults;



}


controller.$inject = [
    '$timeout',
    'ServiceBroker',
    'DisplayNameService'
];


const component = {
    bindings,
    controller,
    template
};


export default {
    component,
    id: 'waltzNavbarSearchForm'
};
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
import {CORE_API} from "../../common/services/core-api-utils";
import {initialiseData} from "../../common/index";

import template from './nav-search-overlay.html';


const bindings = {
    query: '@',
    onDismiss: '<',
    visible: '<'
};


const initialState = {
    categories: [
        'APPLICATION',
        'PERSON',
        'ACTOR',
        'CHANGE_INITIATIVE',
        'DATA_TYPE',
        'APP_GROUP',
        'ORG_UNIT',
        'MEASURABLE',
        'PHYSICAL_SPECIFICATION'
    ],
    selectedCategory: null,
    results: {},
    filteredResults: []
};


function isDescendant(parent, child) {
    let node = child.parentNode;
    while (node != null) {
        if (node == parent) {
            return true;
        }
        node = node.parentNode;
    }
    return false;
}



function controller($element,
                    $document,
                    $timeout,
                    serviceBroker,
                    displayNameService) {
    const vm = initialiseData(this, initialState);

    const documentClick = (e) => {
        const element = $element[0];
        if(!isDescendant(element, e.target)) {
            vm.dismiss();
        }
    };

    vm.$onChanges = c => {
        if(vm.visible) {
            const input = $element.find('input')[0];
            input.focus();
            $timeout(() => $document.on('click', documentClick), 200);
            $timeout(() => $element.on('keydown', vm.onOverlayKeypress), 200);
        }  else {
            $document.off('click', documentClick);
            $element.off('keydown', vm.onOverlayKeypress);
        }
    };

    vm.$onDestroy = () => {
        $document.off('click', documentClick);
    };

    vm.dismiss = () => {
        if (vm.onDismiss) {
            vm.onDismiss();
        } else {
            console.log('No dismiss handler registered');
        }
    };

    vm.toggleCategory = (c) => {
        if ((vm.results[c] || []).length === 0) {
            return;
        }
        if (vm.selectedCategory === c) {
            vm.selectedCategory = null;
        } else {
            vm.selectedCategory = c;
        }
    };

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
            .then(r => vm.results.APP_GROUP = prepareResults(groups, q));

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
            .then(r => vm.results[entityKind] = _.map(r.data, transformResult));
    };


    const doSearch = (query) => {
        if(!query){
            vm.clearSearch();
            return;
        }

        handleSearch(query, CORE_API.ApplicationStore.search, 'APPLICATION');
        handleSearch(query, CORE_API.ChangeInitiativeStore.search, 'CHANGE_INITIATIVE');
        handleSearch(query, CORE_API.DataTypeStore.search, 'DATA_TYPE');
        handleSearch(query, CORE_API.PersonStore.search, 'PERSON');
        handleSearch(query, CORE_API.MeasurableStore.search, 'MEASURABLE');
        handleSearch(query, CORE_API.OrgUnitStore.search, 'ORG_UNIT');
        handleSearch(query, CORE_API.ActorStore.search, 'ACTOR');
        handleSearch(query, CORE_API.PhysicalSpecificationStore.search, 'PHYSICAL_SPECIFICATION');

        searchAppGroups(query);
    };

    vm.doSearch = () => doSearch(vm.query);

    vm.clearSearch = () => {
        vm.results = {};
        vm.query = '';
    };

    vm.onKeypress = (evt) => {
        if(evt.keyCode === 27) {
            if(vm.query) {
                vm.clearSearch();
            }
            else {
                vm.dismiss();
            }
        }
        evt.stopPropagation();
    };

    vm.onOverlayKeypress = (evt) => {
        if(evt.keyCode === 27) {
            vm.dismiss();
        }
    };

}


controller.$inject = [
    '$element',
    '$document',
    '$timeout',
    'ServiceBroker',
    'DisplayNameService'
];


const component = {
    template,
    bindings,
    controller,
};


export default {
    component,
    id: 'waltzNavSearchOverlay'
};

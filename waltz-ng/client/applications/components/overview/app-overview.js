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

import _ from 'lodash';
import {initialiseData} from "../../../common/index";
import {CORE_API} from "../../../common/services/core-api-utils";


import template from './app-overview.html';


const bindings = {
    parentEntityRef: '<'
};


const initialState = {
    aliases: [],
    app: null,
    appGroups: [],
    complexity: null,
    organisationalUnit: null,
    tags: [],
    visibility: {
        aliasEditor: false,
        tagEditor: false
    }
};


function controller($state, serviceBroker) {
    const vm = initialiseData(this, initialState);

    function loadApp() {
        return serviceBroker
            .loadViewData(
                CORE_API.ApplicationStore.getById,
                [vm.parentEntityRef.id])
            .then(r => vm.app = r.data);
    }

    function loadAliases() {
        serviceBroker
            .loadViewData(
                CORE_API.AliasStore.getForEntity,
                [vm.parentEntityRef])
            .then(r => vm.aliases = r.data);
    }

    function loadTags() {
        serviceBroker
            .loadViewData(
                CORE_API.EntityTagStore.findTagsByEntityRef,
                [vm.parentEntityRef])
            .then(r => vm.tags = r.data);
    }

    function loadComplexity() {
        serviceBroker
            .loadViewData(
                CORE_API.ComplexityStore.findBySelector,
                [{ entityReference: vm.parentEntityRef, scope: 'EXACT' }])
            .then(r => vm.complexity = _.get(r.data, '[0]'));
    }

    function loadOrganisationalUnit() {
        serviceBroker
            .loadAppData(
                CORE_API.OrgUnitStore.getById,
                [vm.app.organisationalUnitId])
            .then(r => vm.organisationalUnit = r.data);
    }

    function loadAppGroups() {
        return serviceBroker
            .loadAppData(
                CORE_API.AppGroupStore.findRelatedByEntityRef,
                [vm.parentEntityRef])
            .then(r => vm.appGroups = r.data);
    }

    vm.$onInit = () => {
        loadApp()
            .then(() => loadComplexity())
            .then(() => loadOrganisationalUnit());
        loadAliases();
        loadTags();
        loadAppGroups();
    };


    vm.showAliasEditor = () => vm.visibility.aliasEditor = true;
    vm.showTagEditor = () => vm.visibility.tagEditor = true;

    vm.dismissAliasEditor = () =>  vm.visibility.aliasEditor = false;
    vm.dismissTagEditor = () => vm.visibility.tagEditor = false;

    vm.tagSelected = (keyword) => {
        const params = { tag: keyword };
        $state.go('main.entity-tag.explorer', params);
    };

    vm.saveAliases = (aliases = []) => {
        return serviceBroker
            .execute(
                CORE_API.AliasStore.update,
                [ vm.parentEntityRef, aliases ])
            .then(r => {
                vm.aliases = r.data;
                vm.dismissAliasEditor();
            });
    };

    vm.saveTags = (tags = []) => {
        return serviceBroker
            .execute(
                CORE_API.EntityTagStore.update,
                [ vm.parentEntityRef, tags ])
            .then(r => {
                vm.tags = r.data;
                vm.dismissTagEditor();
            });
    };
}


controller.$inject = [
    '$state',
    'ServiceBroker',
];


const component = {
    bindings,
    template,
    controller
};


export default {
    component,
    id: 'waltzAppOverview'
};

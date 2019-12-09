/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017, 2018, 2019 Waltz open source project
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

import {initialiseData} from "../../../common/index";
import {CORE_API} from "../../../common/services/core-api-utils";
import {determineDownwardsScopeForKind, mkSelectionOptions} from "../../../common/selector-utils";
import {entity} from "../../../common/services/enums/entity";
import {hierarchyQueryScope} from "../../../common/services/enums/hierarchy-query-scope";

import {entityLifecycleStatus} from "../../../common/services/enums/entity-lifecycle-status";

import template from "./auth-sources-section.html";

const bindings = {
    filters: "<",
    parentEntityRef: "<",
    showNonAuthSources: "@?"
};

const allTabDefinitions = [
    {
        name: "Summary",
        template: "wass-summary-tab-content",
        excludeFor: ["DATA_TYPE"]
    }, {
        name: "Rated Flows Scorecard",
        template: "wass-scorecard-tab-content"
    }, {
        name: "Authoritative Sources",
        template: "wass-sources-tab-content"
    }, {
        name: "Non Authoritative Sources",
        template: "wass-nonsources-tab-content"
    }
];


const initialState = {
    authSources: [],
    visibility: {
        sourceDataRatingsOverlay: false,
        authSourcesList: false
    },
    tabDefinitions: [],
    selectedTabName:null
};


function mkTabDefinitionsForKind(kind) {
    return _.reject(
        allTabDefinitions,
        td => _.includes(td.excludeFor, kind));
}


function controller(serviceBroker) {
    const vm = initialiseData(this, initialState);

    const mkSelector = () => {
        const scope = vm.parentEntityRef.kind === entity.ORG_UNIT.key
            ? hierarchyQueryScope.PARENTS.key
            : determineDownwardsScopeForKind(vm.parentEntityRef.kind);

        return mkSelectionOptions(
            vm.parentEntityRef,
            scope,
            [entityLifecycleStatus.ACTIVE.key],
            vm.filters);
    };

    const loadNonAuthSources = () => {
        const selector = mkSelector();
        serviceBroker
            .loadViewData(
                CORE_API.AuthSourcesStore.findNonAuthSources,
                [selector])
            .then(r => vm.nonAuthSources = r.data);
    };

    const loadAuthSources = () => {
        const selector = mkSelector();
        serviceBroker
            .loadViewData(
                CORE_API.AuthSourcesStore.findAuthSources,
                [selector])
            .then(r => {
                vm.authSources = r.data;
            });
    };

    vm.$onInit = () => {
        vm.tabDefinitions = mkTabDefinitionsForKind(vm.parentEntityRef.kind);
        vm.selectedTabName = _.first(vm.tabDefinitions).name;
        loadAuthSources();
        loadNonAuthSources();
    };


    vm.activeTab = () => {
        return _.find(
            vm.tabDefinitions,
            td => td.name === vm.selectedTabName);
    };

    vm.toggleSourceDataRatingOverlay = () =>
        vm.visibility.sourceDataRatingsOverlay = !vm.visibility.sourceDataRatingsOverlay;

}


controller.$inject = ["ServiceBroker"];


export const component = {
    bindings,
    controller,
    template
};


export const id = "waltzAuthSourcesSection";
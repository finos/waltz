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

    const mkSelector = (useDefaultScopeForOrgUnit = true) => {
        const scope = vm.parentEntityRef.kind === entity.ORG_UNIT.key && useDefaultScopeForOrgUnit
            ? hierarchyQueryScope.PARENTS.key
            : determineDownwardsScopeForKind(vm.parentEntityRef.kind);

        return mkSelectionOptions(
            vm.parentEntityRef,
            scope,
            [entityLifecycleStatus.ACTIVE.key],
            vm.filters);
    };

    const loadNonAuthSources = () => {
        const selector = mkSelector(false);
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
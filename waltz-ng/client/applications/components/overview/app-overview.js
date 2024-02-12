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
import {initialiseData} from "../../../common/index";
import {CORE_API} from "../../../common/services/core-api-utils";

import toasts from "../../../svelte-stores/toast-store";
import template from "./app-overview.html";
import {displayError} from "../../../common/error-utils";
import {enrichComplexitiesWithKind, findDefaultComplexityKind} from "../../../complexity/services/complexity-utilities";
import {mkSelectionOptions} from "../../../common/selector-utils";
import PrimaryRatingOverviewSubSection from "../../../measurable-rating/svelte/PrimaryRatingOverviewSubSection.svelte";

const bindings = {
    parentEntityRef: "<"
};


const initialState = {
    aliases: [],
    app: null,
    appGroups: [],
    appGroupsToDisplay: [],
    complexity: null,
    organisationalUnit: null,
    tags: [],
    visibility: {
        aliasEditor: false,
        tagEditor: false
    },
    showAllAppGroups: false,
    PrimaryRatingOverviewSubSection
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
                CORE_API.TagStore.findTagsByEntityRef,
                [vm.parentEntityRef])
            .then(r => vm.tags = r.data);
    }

    function loadComplexities() {
        return serviceBroker
            .loadViewData(
                CORE_API.ComplexityKindStore.findBySelector,
                ["APPLICATION", mkSelectionOptions(vm.parentEntityRef)])
            .then(r => vm.complexityKinds = r.data)
            .then(() => serviceBroker
                .loadViewData(CORE_API.ComplexityStore.findByEntityReference, [vm.parentEntityRef])
                .then(r => {
                    const defaultKindId = _.get(findDefaultComplexityKind(vm.complexityKinds), "id", null);
                    vm.complexities = enrichComplexitiesWithKind(r.data, vm.complexityKinds);
                    vm.overviewComplexity = _.find(vm.complexities, d => d.complexityKindId === defaultKindId);
                }))
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
            .then(r => {
                vm.appGroups = _.orderBy(r.data, ["appGroupKind", "name"], ["desc", "asc"]);

                if (vm.showAllAppGroups){
                    vm.appGroupsToDisplay = vm.appGroups
                } else {
                    vm.appGroupsToDisplay = _.filter(vm.appGroups, r => vm.appGroups.indexOf(r) < 10);
                }
            });
    }

    vm.$onInit = () => {
        loadApp()
            .then(() => loadComplexities())
            .then(() => loadOrganisationalUnit());
        loadAliases();
        loadTags();
        loadAppGroups();
    };

    vm.showAliasEditor = () => vm.visibility.aliasEditor = true;
    vm.showTagEditor = () => vm.visibility.tagEditor = true;

    vm.dismissAliasEditor = () =>  vm.visibility.aliasEditor = false;
    vm.dismissTagEditor = () => vm.visibility.tagEditor = false;

    vm.saveAliases = (aliases = []) => serviceBroker
        .execute(
            CORE_API.AliasStore.update,
            [ vm.parentEntityRef, aliases ])
        .then(r => {
            toasts.success("Updated aliases");
            vm.aliases = r.data
        })
        .catch(e => displayError("Could not update aliases", e));

    vm.saveTags = (tags = [], successMessage) => serviceBroker
        .execute(
            CORE_API.TagStore.update,
            [ vm.parentEntityRef, tags ])
        .then(r => {
            toasts.success(successMessage);
            vm.tags = r.data;
        })
        .catch(e => displayError("Could not update tags", e));

    vm.toggleAppGroupDisplay = () => {
        vm.showAllAppGroups = !vm.showAllAppGroups;
        loadAppGroups();
    };
}


controller.$inject = [
    "$state",
    "ServiceBroker"
];


const component = {
    bindings,
    template,
    controller
};


export default {
    component,
    id: "waltzAppOverview"
};

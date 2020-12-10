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

import {initialiseData} from "../../../common";
import {dynamicSections} from "../../../dynamic-section/dynamic-section-definitions";

import template from "./licence-view.html";
import {CORE_API} from "../../../common/services/core-api-utils";


const bindings = {
};


const addToHistory = (historyStore, licence) => {
    if (! licence) { return; }
    historyStore.put(
        licence.name,
        "LICENCE",
        "main.licence.view",
        { id: licence.id });
};


const initialState = {
    appsSection: dynamicSections.appsSection,
    bookmarkSection: dynamicSections.bookmarksSection,
    changeLogSection: dynamicSections.changeLogSection,
    entityNamedNotesSection: dynamicSections.entityNamedNotesSection,
    softwarePackagesSection: dynamicSections.softwarePackagesSection,
};


function controller($stateParams, historyStore, serviceBroker) {
    const vm = initialiseData(this, initialState);

    const loadLicence = () => {
        return serviceBroker
            .loadViewData(
                CORE_API.LicenceStore.getById,
                [vm.parentEntityRef.id])
            .then(r => vm.licence = r.data);
    };


    vm.$onInit = () => {
        vm.licenceId = $stateParams.id;
        vm.parentEntityRef = {
            kind: "LICENCE",
            id: vm.licenceId
        };

        loadLicence()
            .then(() => addToHistory(historyStore, vm.licence));
    };
}


controller.$inject = [
    "$stateParams",
    "HistoryStore",
    "ServiceBroker"
];


const component = {
    bindings,
    controller,
    template
};


export default {
    id: "waltzLicenceView",
    component
};




/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017, 2018, 2019  Waltz open source project
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




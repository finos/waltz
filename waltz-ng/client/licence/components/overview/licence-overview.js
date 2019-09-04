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

import { CORE_API } from "../../../common/services/core-api-utils";
import { initialiseData } from "../../../common";

import template from "./licence-overview.html";


const bindings = {
    parentEntityRef: "<",
};


const initialState = {
    aliases: [],
    licence: null,
    visibility: {
        aliasEditor: false
    }
};


function controller(serviceBroker) {
    const vm = initialiseData(this, initialState);

    const loadLicence = () => {
        return serviceBroker
            .loadViewData(
                CORE_API.LicenceStore.getById,
                [vm.parentEntityRef.id])
            .then(r => vm.licence = r.data);
    };

    const loadAliases = () => {
        return serviceBroker
            .loadViewData(
                CORE_API.AliasStore.getForEntity,
                [vm.parentEntityRef])
            .then(r => vm.aliases = r.data);
    };

    vm.showAliasEditor = () => vm.visibility.aliasEditor = true;
    vm.dismissAliasEditor = () =>  vm.visibility.aliasEditor = false;

    vm.saveAliases = (aliases = []) => serviceBroker
        .execute(
            CORE_API.AliasStore.update,
            [ vm.parentEntityRef, aliases ])
        .then(r =>  vm.aliases = r.data);


    vm.$onInit = () => {
        loadLicence();
        loadAliases();
    };
}


controller.$inject = [
    "ServiceBroker"
];


const component = {
    template,
    bindings,
    controller
};


export default {
    component,
    id: "waltzLicenceOverview"
};

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
import { CORE_API } from "../../../common/services/core-api-utils";
import template from "./change-initiative-view.html";
import { initialiseData } from "../../../common";


const initialState = {
    changeInitiative: {},
    related: {
        appGroupRelationships: []
    },
    orgUnit: null,
    entityRef: null
};


function controller($stateParams,
                    dynamicSectionManager,
                    historyStore,
                    serviceBroker) {

    const {id} = $stateParams;
    const vm = initialiseData(this, initialState);

    vm.entityRef = {
        kind: "CHANGE_INITIATIVE",
        id: id
    };

    vm.$onInit = () => {
        const ciPromise = serviceBroker
            .loadViewData(CORE_API.ChangeInitiativeStore.getById, [id])
            .then(result => {
                vm.changeInitiative = result.data;
                return vm.changeInitiative;
            });

        ciPromise
            .then((ci) => serviceBroker
                .loadViewData(
                    CORE_API.OrgUnitStore.getById,
                    [ ci.organisationalUnitId ])
                .then( r => vm.orgUnit = r.data));

        ciPromise
            .then((ci) => historyStore
                .put(
                    ci.name,
                    "CHANGE_INITIATIVE",
                    "main.change-initiative.view",
                    { id: ci.id }));

        dynamicSectionManager.initialise("CHANGE_INITIATIVE");

    };

}


controller.$inject = [
    "$stateParams",
    "DynamicSectionManager",
    "HistoryStore",
    "ServiceBroker"
];


const page = {
    template,
    controller,
    controllerAs: "ctrl"
};


export default page;


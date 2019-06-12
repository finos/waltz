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

import { initialiseData } from "../../../common/index";
import { mkRef } from "../../../common/entity-utils";
import { CORE_API } from "../../../common/services/core-api-utils";

import template from "./person-view.html";


const initialState = {
    filters: {},
};


function controller($stateParams,
                    dynamicSectionManager,
                    historyStore,
                    serviceBroker) {

    const vm = initialiseData(this, initialState);




    vm.$onInit = () => {
        const id = $stateParams.id;

        if (id) {
            vm.entityRef = mkRef("PERSON",  _.parseInt(id));
            serviceBroker
                .loadViewData(
                    CORE_API.PersonStore.getById,
                    [ id ])
                .then(r => r.data)
                .then(person => {

                    if (!person) {
                        return;
                    }
                    vm.person = person;

                    dynamicSectionManager.initialise("PERSON");
                    historyStore.put(
                        person.displayName,
                        "PERSON",
                        "main.person.view",
                        { empId: person.employeeId });

                });
        }

    };

    // -- INTERACT --
    vm.filtersChanged = (filters) => {
        vm.filters = filters;
    };

}


controller.$inject = [
    "$stateParams",
    "DynamicSectionManager",
    "HistoryStore",
    "ServiceBroker"
];


export default {
    template,
    controller,
    controllerAs: "ctrl"
};

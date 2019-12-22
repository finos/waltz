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

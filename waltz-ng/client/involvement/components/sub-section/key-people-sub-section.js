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

import { initialiseData } from "../../../common";

import template from "./key-people-sub-section.html";
import { CORE_API } from "../../../common/services/core-api-utils";

const bindings = {
    parentEntityRef: "<",
};

const initialState = {
    keyPeople: [],
};

function controller($q, serviceBroker) {

    const vm = initialiseData(this, initialState);

    const refresh = () => {
        return serviceBroker
            .loadViewData(CORE_API.InvolvementViewService.findKeyInvolvementsForEntity, [vm.parentEntityRef])
            .then(r => r.data)
            .then(keyInvolvements => vm.keyPeople = _
                .chain(keyInvolvements)
                .groupBy(d => d.involvementKind.name)
                .map((v, k) => ({
                    roleName: k,
                    persons: _.map(v, d => d.person)
                }))
                .value());
    };

    vm.$onChanges = () => {
        if (vm.parentEntityRef) {
            refresh();
        }
    };
}

controller.$inject = [
    "$q",
    "ServiceBroker"
];


const component = {
    template,
    bindings,
    controller
};

export default {
    component,
    id: "waltzKeyPeopleSubSection"
};

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
import { aggregatePeopleByKeyInvolvementKind } from "../../involvement-utils";
import { dynamicSections } from "../../../dynamic-section/dynamic-section-definitions";

const bindings = {
    parentEntityRef: "<",
};

const initialState = {
    keyPeople: []
};

function controller($q, serviceBroker, dynamicSectionManager) {

    const vm = initialiseData(this, initialState);

    const refresh = () => {
        const involvementPromise = serviceBroker
            .loadViewData(
                CORE_API.InvolvementStore.findByEntityReference,
                [ vm.parentEntityRef ])
            .then(r => r.data);

        const peoplePromise = serviceBroker
            .loadViewData(
                CORE_API.InvolvementStore.findPeopleByEntityReference,
                [ vm.parentEntityRef ])
            .then(r => r.data);

        const keyInvolvementsPromise = serviceBroker
            .loadViewData(
                CORE_API.InvolvementKindStore.findKeyInvolvementKindsByEntityKind,
                [ vm.parentEntityRef.kind ])
            .then(r => r.data);

        $q.all([involvementPromise, peoplePromise, keyInvolvementsPromise])
            .then(([involvements = [], people = [], keyInvolvementKinds = []]) => {
                vm.keyPeople = aggregatePeopleByKeyInvolvementKind(involvements, people, keyInvolvementKinds);
            });
    };

    vm.$onChanges = () => {
        if (vm.parentEntityRef) {
            refresh();
        }
    };

    vm.onSelect = () => {
        dynamicSectionManager.activate(dynamicSections.involvedPeopleSection);
    };
}

controller.$inject = [
    "$q",
    "ServiceBroker",
    "DynamicSectionManager"
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

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
import template from "./legal-entity-section.html";
import {initialiseData} from "../../../common";
import {CORE_API} from "../../../common/services/core-api-utils";
import _ from "lodash";
import {mkSelectionOptions} from "../../../common/selector-utils";

const bindings = {
    parentEntityRef: "<"
};

const initialState = {
    relationshipKinds: [],
    visibility: {
        tab: null
    }
}


function controller($q, serviceBroker) {

    const vm = initialiseData(this, initialState);


    vm.$onChanges = () => {

        vm.selectionOptions = mkSelectionOptions(vm.parentEntityRef);

        serviceBroker
            .loadViewData(CORE_API.LegalEntityRelationshipKindStore.findAll)
            .then(r => vm.relationshipKinds = r.data)
            .then(r => vm.visibility.tab = vm.visibility.tab || _.get(_.first(vm.relationshipKinds), "id", null));
    }

    vm.onTabChange = (c) => {
    }
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
    id: "waltzLegalEntitySection",
    component
};
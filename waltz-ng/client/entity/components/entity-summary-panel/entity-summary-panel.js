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
import template from "./entity-summary-panel.html";
import {initialiseData} from "../../../common";
import {CORE_API} from "../../../common/services/core-api-utils";


const bindings = {
    entityRef: "<"
};


const initialState = {

};


const loaders = {
    "APPLICATION": CORE_API.ApplicationStore.getById,
    "ACTOR": CORE_API.ActorStore.getById,
};


function controller(serviceBroker) {
    const vm = initialiseData(this, initialState);

    function loadEntity() {
        return serviceBroker
            .loadViewData(
                loaders[vm.entityRef.kind],
                [ vm.entityRef.id ])
            .then(r => vm.entity = r.data);
    }

    function load() {
        loadEntity()
            .then(entity => {
                if (entity.kind === "APPLICATION") {
                    serviceBroker
                        .loadViewData(
                            CORE_API.OrgUnitStore.getById,
                            [vm.entity.organisationalUnitId])
                        .then(r => vm.orgUnit = r.data);
                }
            });
    }

    vm.$onChanges = (c) => {
        if (vm.entityRef && c.entityRef) {
            load();
        }
    }
}

controller.$inject = ["ServiceBroker"];


const component = {
    bindings,
    template,
    controller
};


export default {
    id: "waltzEntitySummaryPanel",
    component
};
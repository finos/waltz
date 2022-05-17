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

import {initialiseData} from "../../common";
import {CORE_API} from "../../common/services/core-api-utils";
import template from "./change-log-section.html";
import {entity} from "../../common/services/enums/entity";


const bindings = {
    parentEntityRef: "<",
    userName: "<"
};


const initialState = {
    entries: [],
    activeTab: "table",
    allowApplicationChangeCalendar: false
};


const allowedAppCalendarKinds = [
    entity.APP_GROUP.key,
    entity.ORG_UNIT.key,
    entity.PROCESS_DIAGRAM.key,
    entity.CHANGE_INITIATIVE.key,
    entity.APPLICATION.key,
    entity.MEASURABLE.key,
    entity.FLOW_DIAGRAM.key,
    entity.LOGICAL_DATA_FLOW.key,
    entity.PHYSICAL_FLOW.key,
    entity.PHYSICAL_SPECIFICATION.key,
    entity.PERSON.key
];


function controller(serviceBroker) {
    const vm = initialiseData(this, initialState);

    const loadEntries = () => {
        const dataLoadHandler = (result) => {
            vm.entries = result.data;
        };

        if (vm.parentEntityRef) {
            serviceBroker
                .loadViewData(
                    CORE_API.ChangeLogStore.findByEntityReference,
                    [vm.parentEntityRef],
                    { force: true })
                .then(dataLoadHandler);
        } else if (vm.userName) {
            serviceBroker
                .loadViewData(
                    CORE_API.ChangeLogStore.findForUserName,
                    [vm.userName],
                    { force: true })
                .then(dataLoadHandler);
        }
    };

    vm.$onChanges = (changes) => {
        if (vm.parentEntityRef || vm.userName) {
            loadEntries();
            vm.allowApplicationChangeCalendar = _.includes(allowedAppCalendarKinds, vm.parentEntityRef.kind);
        }
    };

    vm.refresh = () => loadEntries();

    vm.changeLogTableInitialised = (api) => {
        vm.exportChangeLog = () => api.exportFn("change-log.csv");
    };
}


controller.$inject = [
    "ServiceBroker"
];


const component = {
    bindings,
    template,
    controller
};


export default component;

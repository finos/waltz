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

import template from "./person-hierarchy-section.html";
import {initialiseData} from "../../../common/index";
import {CORE_API} from "../../../common/services/core-api-utils";
import _ from "lodash";

const bindings = {
    parentEntityRef: "<"
};


const initialState = {
    cumulativeCount: null,
    kindCount: null,
    kindKeys: null
};


function controller(serviceBroker) {
    const vm = initialiseData(this, initialState);

    vm.$onInit = () => {
        serviceBroker
            .loadViewData(
                CORE_API.PersonStore.getById,
                [ vm.parentEntityRef.id ])
            .then(r => r.data.employeeId)
            .then(empId => {
                serviceBroker
                    .loadViewData(CORE_API.PersonStore.findDirects, [ empId ])
                    .then(r => vm.directs = r.data);

                serviceBroker
                    .loadViewData(CORE_API.PersonStore.findManagers, [ empId ])
                    .then(r => vm.managers = r.data);

                serviceBroker
                    .loadViewData(CORE_API.PersonStore.countCumulativeReportsByKind, [ empId ])
                    .then(r =>{
                        vm.kindKeys = _.keys(r.data);
                        vm.kindCounts = r.data;
                        vm.cumulativeCount = _.sum(_.values(r.data));
                    })
            });
    };
}

controller.$inject = [ "ServiceBroker" ];


const component = {
    bindings,
    template,
    controller
};


const id = "waltzPersonHierarchySection";


export default {
    id,
    component
};
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
import _ from "lodash";
import {mapToDisplayNames} from "../application-utils";

import template from "./app-table.html";

const bindings = {
    applications: "<"
};


function mkGridData(displayNameService, apps = []) {
    return _.map(apps || [], a => Object.assign(
        {},
        a,
        mapToDisplayNames(displayNameService, a))
    );
}


const columnDefs = [
    {
        field: "name",
        cellTemplate: `<div class="ui-grid-cell-contents"
                            ng-switch="row.entity['management']">
                         <span ng-switch-when="End User"
                               ng-bind="COL_FIELD"></span>
                         <span ng-switch-default>
                            <waltz-entity-link tooltip-placement="right"
                                               entity-ref="row.entity">
                            </waltz-entity-link>
                         </span>
                       </div>`
    },
    { field: "assetCode"},
    { field: "kindDisplay", name: "Kind"},
    { field: "overallRatingDisplay", name: "Overall Rating"},
    { field: "riskRatingDisplay", name: "Risk Rating"},
    { field: "businessCriticalityDisplay", name: "Business Criticality"},
    { field: "lifecyclePhaseDisplay", name: "Lifecycle Phase"}
];


function controller(displayNameService) {

    const vm = this;

    vm.columnDefs = columnDefs;

    vm.$onChanges= () => vm.gridData = mkGridData(displayNameService, vm.applications);
}


controller.$inject = ["DisplayNameService"];


const component = {
    template,
    bindings,
    controller
};


export default component;

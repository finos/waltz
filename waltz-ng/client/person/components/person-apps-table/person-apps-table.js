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
import {mapToDisplayNames} from "../../../applications/application-utils";

import template from "./person-apps-table.html";

const bindings = {
    applications: "<"
};


function mkGridData(displayNameService, apps = []) {
    return _.map(apps || [], a => {

        const roles = _.chain(a.roles)
            .map(d => d.name)
            .sort()
            .join(", ")
            .value();

        return Object.assign(
            {},
            a,
            mapToDisplayNames(displayNameService, a),
            { displayRoles: roles})}
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
    { field: "lifecyclePhaseDisplay", name: "Lifecycle Phase"},
    { field: "displayRoles",
        name: "Roles",
        cellTemplate: `
                <div class="ui-grid-cell-contents">
                    <span ng-bind="COL_FIELD"
                          uib-popover-template="'wpat/role-popup.html'"
                          popover-trigger="mouseenter"
                          popover-append-to-body="true">
                    </span>
                </div>`,
        width: '20%'
    }
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


const id = "waltzPersonAppsTable";


export default {
    component,
    id
};

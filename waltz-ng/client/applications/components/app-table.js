/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017 Waltz open source project
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
import _ from "lodash";
import {mapToDisplayNames} from "../application-utils";
import template from  './app-table.html';

const bindings = {
    applications: '<',
    onInitialise: '<'
};


function mkGridData(apps = []) {
    return _.map(apps || [], a => Object.assign(
            {},
            a,
            mapToDisplayNames(a))
    );
}


const columnDefs = [
    {
        field: 'name',
        cellTemplate: `<div class="ui-grid-cell-contents" 
                            ng-switch="row.entity['management']">
                         <span ng-switch-when="End User" 
                               ng-bind="COL_FIELD"></span>
                         <a ng-switch-default
                            ui-sref="main.app.view ({ id: row.entity['id'] })" 
                            ng-bind="COL_FIELD">
                         </a>
                       </div>`
    },
    { field: 'assetCode'},
    { field: 'kindDisplay', name: 'Kind'},
    { field: 'overallRatingDisplay', name: 'Overall Rating'},
    { field: 'riskRatingDisplay', name: 'Risk Rating'},
    { field: 'businessCriticalityDisplay', name: 'Business Criticality'},
    { field: 'lifecyclePhaseDisplay', name: 'Lifecycle Phase'}
];


function controller() {

    const vm = this;

    vm.columnDefs = columnDefs;

    vm.$onChanges= () => vm.gridData = mkGridData(vm.applications);
}


controller.$inject = [
];


const component = {
    template,
    bindings,
    controller
};


export default component;

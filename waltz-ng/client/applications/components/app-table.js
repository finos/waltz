/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016  Khartec Ltd.
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
import {
    criticalityDisplayNames,
    investmentRatingNames,
    lifecyclePhaseDisplayNames,
    applicationKindDisplayNames
} from "../../common/services/display-names";


const bindings = {
    applications: '<',
    onInitialise: '<'
};


function processApps(apps = []) {
    return _.chain(apps || [])
        .map(a => Object.assign(
            {},
            a,
            {
                kind: applicationKindDisplayNames[a.kind] || a.kind,
                overallRating: investmentRatingNames[a.overallRating] || a.overallRating,
                businessCriticality: criticalityDisplayNames[a.businessCriticality] || a.businessCriticality,
                riskRating: criticalityDisplayNames[a.riskRating] || a.riskRating,
                lifecyclePhase: lifecyclePhaseDisplayNames[a.lifecyclePhase] || a.lifecyclePhase
            }))
        .value();
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
    { field: 'kind'},
    { field: 'overallRating'},
    { field: 'riskRating'},
    { field: 'businessCriticality'},
    { field: 'lifecyclePhase'}
];


function controller() {

    const vm = this;

    vm.columnDefs = columnDefs;

    vm.$onChanges= () => vm.gridData = processApps(vm.applications);
}


controller.$inject = [
];


const component = {
    template: require('./app-table.html'),
    bindings,
    controller
};


export default component;

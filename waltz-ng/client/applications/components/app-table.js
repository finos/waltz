/*
 *  Waltz
 * Copyright (c) David Watkins. All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 *
 */
import _ from "lodash";
import {
    criticalityDisplayNames,
    investmentRatingNames,
    lifecyclePhaseDisplayNames,
    applicationKindDisplayNames
} from "../../common/services/display_names";


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

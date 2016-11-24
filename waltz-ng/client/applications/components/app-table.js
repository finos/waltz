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


const bindings = {
    applications: '<',
    onInitialise: '<'
};


const columnDefs = [
    {
        width: 40,
        field: 'info',
        displayName: '',
        cellTemplate: `<div class="ui-grid-cell-contents text-center"> 
                         <a class="clickable" 
                            ng-click="grid.appScope.$ctrl.onAppSelect(row.entity)"> 
                            <waltz-icon name="info-circle" 
                                        size="lg"></waltz-icon>
                         </a>
                       </div>`
    }, {
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
    }, {
        field: 'assetCode'
    },{
        field: 'name',
        cellTemplate: `<div class="ui-grid-cell-contents">
                          <span ng-bind="row.entity['management'] "></span>
                          <span ng-bind="COL_FIELD"></span>
                       </div>`
    }, {
        field: 'kind',
        cellFilter: "toDisplayName:'applicationKind'",
    }, {
        field: 'overallRating',
        cellFilter: "toDisplayName:'investmentRating'",
    }, {
        field: 'riskRating',
        cellFilter: "toDisplayName:'criticality'",
    }, {
        field: 'businessCriticality',
        cellFilter: "toDisplayName:'criticality'",
    }, {
        field: 'lifecyclePhase',
        cellFilter: "toDisplayName:'lifecyclePhase'",
    }
];


function controller() {

    const vm = this;

    vm.columnDefs = columnDefs;

    vm.$onChanges= () => vm.gridData = vm.applications || [];
}


controller.$inject = [
];


const component = {
    template: require('./app-table.html'),
    bindings,
    controller
};


export default component;

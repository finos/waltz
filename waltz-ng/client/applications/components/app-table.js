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
    lifecyclePhaseDisplayNames,
    applicationKindDisplayNames,
    investmentRatingNames
} from "../../common/services/display_names";


const bindings = {
    applications: '<',
    csvName: '@?'
};


function controller(uiGridConstants, $animate) {

    const vm = this;

    const csvName = angular.isDefined(vm.csvName) ? vm.csvName : 'applications.csv';
    vm.selectedApp = undefined;

    vm.gridOptions = {
        enableGridMenu: true,
        exporterCsvFilename: csvName,
        exporterMenuPdf: false,
        enableSorting: true,
        enableFiltering: true,
        enableHorizontalScrollbar: uiGridConstants.scrollbars.NEVER,
        onRegisterApi: (gridApi) => {
            $animate.enabled(gridApi.grid.element, false);
        },
        columnDefs: [
            {
                field: 'info',
                displayName: '',
                enableSorting: false,
                enableColumnMenu: false,
                enableFiltering: false,
                width: 40,
                cellTemplate: '<div class="ui-grid-cell-contents text-center"> \n    <a class="clickable" \n       ng-click="grid.appScope.$ctrl.onAppSelect(row.entity)"> \n        <waltz-icon name="info-circle" \n                    size="lg">\n        </waltz-icon>\n    </a>\n</div>'
            }, {
                field: 'name',
                cellTemplate: '<div class="ui-grid-cell-contents" ng-switch="row.entity[\'management\']">\n    <span ng-switch-when="row.entity[\'management\'] == \'End User\'" \n          ng-bind="COL_FIELD">\n    </span>\n    <a ng-switch-default\n       ui-sref="main.app.view ({ id: row.entity[\'id\'] })" \n       ng-bind="COL_FIELD">\n    </a>\n</div>'
            }, {
                field: 'assetCode'
            }, {
                field: 'kind',
                cellTemplate: '<div class="ui-grid-cell-contents"><span ng-bind="COL_FIELD | toDisplayName:\'applicationKind\'"></span></div>',
                filter: {
                    type: uiGridConstants.filter.SELECT,
                    selectOptions: _.map(applicationKindDisplayNames, (label, value) => ({ label, value }))
                }
            }, {
                field: 'overallRating',
                cellTemplate: '<div class="ui-grid-cell-contents"><span ng-bind="COL_FIELD | toDisplayName:\'investmentRating\'"></span></div>',
                filter: {
                    type: uiGridConstants.filter.SELECT,
                    selectOptions: _.map(investmentRatingNames, (label, value) => ({ label, value }))
                }
            }, {
                field: 'lifecyclePhase',
                cellTemplate: '<div class="ui-grid-cell-contents"><span ng-bind="COL_FIELD | toDisplayName:\'lifecyclePhase\'"></span></span></div>',
                filter: {
                    type: uiGridConstants.filter.SELECT,
                    selectOptions: _.map(lifecyclePhaseDisplayNames, (label, value) => ({ label, value }))
                }
            }
        ],
        data: vm.applications
    };

    vm.onAppSelect = app => {
        vm.selectedApp = app;
    };

    vm.$onChanges= () => vm.gridOptions.data = vm.applications || [];
}


controller.$inject = [
    'uiGridConstants',
    '$animate'
];


const component = {
    template: require('./app-table.html'),
    bindings,
    controller
};


export default component;


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
import {authoritativeRatingBackgroundColorScale} from "../../../common/colors";
import {mkEntityLinkGridCell} from "../../../common";


const bindings = {
    flows: '<',
    decorators: '<'
};


function controller($animate,
                    $scope,
                    displayNameService,
                    uiGridConstants) {
    const vm = this;

    vm.$onChanges = (change) => {
        vm.gridOptions = setupGrid($animate, displayNameService, uiGridConstants, vm.flows, vm.decorators, $scope);
    };
}


controller.$inject = [
    '$animate',
    '$scope',
    'WaltzDisplayNameService',
    'uiGridConstants'
];


const template = `<div style="font-size: smaller; height: 300px"
                       ui-grid-exporter
                       ui-grid-resize-columns
                       ui-grid="$ctrl.gridOptions">
                  </div>
                   <span class="custom-csv-link-location"></span>`;


const component = {
    bindings,
    template,
    controller
};


function groupDecoratorsByFlowId(decorators = [], displayNameService) {
    return _.chain(decorators)
        .filter(dc => dc.decoratorEntity.kind === 'DATA_TYPE')
        .map(dc => _.assign({}, {
            dataFlowId: dc.dataFlowId,
            dataType: {
                id: dc.decoratorEntity.id,
                name: displayNameService.lookup('dataType', dc.decoratorEntity.id),
                kind: 'DATA_TYPE'
            },
            authSourceRating: dc.rating
        }))
        .groupBy('dataFlowId')
        .value();
}


function prepareGridData(flows = [], decorators = [], displayNameService) {
    const groupedDecorators = groupDecoratorsByFlowId(decorators, displayNameService);
    return _.flatMap(flows,
        flow => _.map(groupedDecorators[flow.id],
            dc => _.assign({
                dataType: dc.dataType,
                authSourceRating: dc.authSourceRating,
                rowStyle: {
                    'background-color': authoritativeRatingBackgroundColorScale(dc.authSourceRating).toString()
                }
            },
            flow)));
}


function setupGrid($animate, displayNameService, uiGridConstants, flows = [], decorators = []) {
    const gridData = prepareGridData(flows, decorators, displayNameService);

    const columnDefs = [
        mkEntityLinkGridCell('Source', 'source', 'none'),
        mkEntityLinkGridCell('Target', 'target', 'none'),
        mkEntityLinkGridCell('Data Type', 'dataType', 'none'),
        { field: 'authSourceRating', displayName: 'Source Rating', cellFilter: 'toDisplayName:"rating"' }
    ];

    return {
        columnDefs,
        data: gridData,
        enableGridMenu: true,
        enableFiltering: true,
        enableSelectAll: true,
        exporterMenuPdf: false,
        exporterCsvFilename: "flows.csv",
        enableHorizontalScrollbar: uiGridConstants.scrollbars.NEVER,
        enableSorting: true,
        rowTemplate: '<div ng-style="row.entity.rowStyle"><div ng-repeat="col in colContainer.renderedColumns track by col.colDef.name" class="ui-grid-cell" ui-grid-cell></div></div>',
        onRegisterApi: (gridApi) => {
            $animate.enabled(gridApi.grid.element, false);
        }
    };
}


export default component;

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


const bindings = {
    flows: '<',
    decorators: '<'
};


function controller($animate,
                    displayNameService,
                    uiGridConstants) {
    const vm = this;

    vm.$onChanges = (change) => {
        vm.gridOptions = setupGrid($animate, displayNameService, uiGridConstants, vm.flows, vm.decorators);
    };
}


controller.$inject = [
    '$animate',
    'WaltzDisplayNameService',
    'uiGridConstants'
];


const template = `<div style="font-size: smaller; height: 300px"
                       ui-grid-exporter
                       ui-grid-resize-columns
                       ui-grid="$ctrl.gridOptions">
                  </div>`;


const component = {
    bindings,
    template,
    controller
};


function mkEntityNameCell(entityDataObjectField, valueField, columnHeading, entityNavViewName) {
    return {
        field: valueField,
        displayName: columnHeading,
        cellTemplate: `<div class="ui-grid-cell-contents">\n<a ui-sref="${entityNavViewName} ({ id: row.entity[\'${entityDataObjectField}\'][\'id\']})" ng-bind="COL_FIELD">\n</a>\n</div>`
    };
}


function groupDecoratorsByFlowId(decorators = [], displayNameService) {
    return _.chain(decorators)
        .filter(dc => dc.decoratorEntity.kind === 'DATA_TYPE')
        .map(dc => _.assign({}, {
            dataFlowId: dc.dataFlowId,
            dataType: {
                id: dc.decoratorEntity.id,
                name: displayNameService.lookup('dataType', dc.decoratorEntity.id)
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
                flow
            )));
}


function setupGrid($animate, displayNameService, uiGridConstants, flows = [], decorators = []) {
    const gridData = prepareGridData(flows, decorators, displayNameService);

    const columnDefs = [
        mkEntityNameCell('source', 'source.name', 'Source', 'main.app.view'),
        mkEntityNameCell('target', 'target.name', 'Target', 'main.app.view'),
        mkEntityNameCell('dataType', 'dataType.name', 'Data Type', 'main.data-type.view'),
        { field: 'authSourceRating', displayName: 'Source Rating', cellFilter: 'toDisplayName:"rating"' }
    ];

    return {
        columnDefs,
        data: gridData,
        enableGridMenu: true,
        enableFiltering: true,
        enableHorizontalScrollbar: uiGridConstants.scrollbars.NEVER,
        enableSorting: true,
        exporterCsvFilename: "flows.csv",
        exporterMenuPdf: false,
        rowTemplate: '<div ng-style="row.entity.rowStyle"><div ng-repeat="col in colContainer.renderedColumns track by col.colDef.name" class="ui-grid-cell" ui-grid-cell></div></div>',
        onRegisterApi: (gridApi) => {
            $animate.enabled(gridApi.grid.element, false);
        }
    };
}


export default component;
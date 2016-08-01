import _ from 'lodash';


/**
 * Intended to show a table similar to:
 *
 *  | App Name (link) | Outcome | Reason | Update Date |
 *
 * This should only be used for values associated
 * with a single entity-statistic-definition.
 **/

const bindings = {
    applications: '<',
    filterOutcome: '<',
    statisticDefinition: '<',
    statisticValues: '<'
};


function controller($animate, uiGridConstants) {
    const vm = this;

    vm.$onChanges = (change) => {
        vm.gridOptions = setupGrid($animate, uiGridConstants, vm.statisticDefinition);

        if (change.statisticValues) {
            vm.gridOptions.data = vm.statisticValues || [];
        }

        if(change.filterOutcome) {
            const tableOutcomeCell = vm.gridOptions.columnDefs[2];
            tableOutcomeCell.filter.term = vm.filterOutcome;
        }

        if(change.applications) {
            vm.appsById = _.keyBy(vm.applications, 'id');
        }

        if (vm.appsById && vm.statisticValues) {
            _.each(vm.statisticValues, sv => sv.application = vm.appsById[sv.entity.id]);
        }
    };
}


controller.$inject = [
    '$animate',
    'uiGridConstants'
];


const template = "<div style=\"font-size: smaller; height: 300px\"\n     ui-grid-exporter\n     ui-grid-resize-columns\n     ui-grid=\"$ctrl.gridOptions\">\n</div>";


const component = {
    controller,
    bindings,
    template
};


const appNameCell = {
    field: 'entity.name',
    displayName: 'Application',
    sort: { direction: 'asc' },
    cellTemplate: '<div class="ui-grid-cell-contents">\n    <a ui-sref="main.app.view ({ id: row.entity[\'entity\'][\'id\']})" ng-bind="COL_FIELD">\n    </a>\n</div>'
};


const outcomeCell = (uiGridConstants) => {
    return {
        field: 'outcome',
        filter: {
            term: null,
            condition: uiGridConstants.filter.EXACT
        }
    }
};


const assetCodeCell = {
    field: 'application.assetCode',
    displayName: 'Asset Code'
};


const valueCell = (uiGridConstants, statisticDefinition) => {
    return {
        field: 'value',
        type: (statisticDefinition && statisticDefinition.type === 'NUMERIC')
            ? 'number'
            : 'string',
        filter: {
            term: null,
            condition: uiGridConstants.filter.EXACT
        }
    }
};


const reasonCell = {
    field: 'reason'
};


const dateCell = {
    field: 'createdAt',
    displayName: 'Last Updated',
    enableFiltering: false,
    cellTemplate: '<div class="ui-grid-cell-contents">\n     <waltz-from-now timestamp="COL_FIELD"></waltz-from-now>\n</div>'
};


function setupGrid($animate, uiGridConstants, statisticDefinition) {
    return {
        enableGridMenu: true,
        exporterCsvFilename: "stats",
        exporterMenuPdf: false,
        enableSorting: true,
        enableFiltering: true,
        enableHorizontalScrollbar: uiGridConstants.scrollbars.NEVER,
        onRegisterApi: (gridApi) => {
            $animate.enabled(gridApi.grid.element, false);
        },
        columnDefs: [
            appNameCell,
            assetCodeCell,
            outcomeCell(uiGridConstants),
            valueCell(uiGridConstants, statisticDefinition),
            reasonCell,
            dateCell
        ]
    };
}


export default component;
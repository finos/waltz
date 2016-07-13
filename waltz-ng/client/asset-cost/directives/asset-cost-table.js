import _ from "lodash";


const DEFAULT_OPTIONS = {
    showAssetCode: true,
    showAppName: false,
    showOrgUnit: false
};


const BINDINGS = {
    costs: '=',
    options: '=?',
    selectedBucket: '=?',
    csvName: '@?'
};


function prepareColumns(displayNameService, uiGridConstants) {
    const kindCol = {
        field: 'cost.kind',
        displayName: 'Cost Type',
        cellFilter: 'toDisplayName:"assetCost"',
        filter: {
            type: uiGridConstants.filter.SELECT,
            selectOptions: displayNameService.toGridOptions('assetCost')
        }
    };

    const amountCol = {
        field: 'cost.amount',
        displayName: 'Amount',
        cellFilter: 'currency:"€"',
        cellClass: 'waltz-grid-currency',
        filters: [
            {
                condition: uiGridConstants.filter.GREATER_THAN,
                placeholder: 'Greater than'
            }, {
                condition: uiGridConstants.filter.LESS_THAN,
                placeholder: 'Less than'
            }
        ]
    };

    const yearCol = {
        field: 'cost.year',
        displayName: 'Year'
    };

    const appCol = {
        field: 'application.name',
        cellTemplate: '<div class="ui-grid-cell-contents"> <a ui-sref="main.app.view ({ id: row.entity[\'application\'][\'id\'] })" ng-bind="COL_FIELD"></a></div>',
        displayName: 'Application'
    };

    const orgCol = {
        field: 'orgUnit.name',
        displayName: 'Org Unit'
    };

    const assetCodeCol = {
        field: 'assetCode'
    };

    return {
        kindCol,
        amountCol,
        yearCol,
        appCol,
        orgCol,
        assetCodeCol
    };
}


function determineColumns(colDefinitions, options) {
    const cols = [
        options.showAssetCode ? colDefinitions.assetCodeCol : null,
        options.showAppName ? colDefinitions.appCol : null,
        options.showOrgUnit ? colDefinitions.orgCol : null,
        colDefinitions.yearCol,
        colDefinitions.kindCol,
        colDefinitions.amountCol
    ];

    return _.compact(cols);
}


function prepareGridOptions(colDefinitions, options, uiGridConstants, $animate) {

    const columns = determineColumns(colDefinitions, options);

    const gridOptions = {
        enableSorting: true,
        enableFiltering: true,
        exporterMenuPdf: false,
        enableHorizontalScrollbar: uiGridConstants.scrollbars.NEVER,
        columnDefs: columns,
        data: [],
        onRegisterApi: (gridApi) => $animate.enabled(gridApi.grid.element, false)
    };

    return gridOptions;
}

function setupExportOptions(options, csvName) {
    Object.assign(options, {
        enableGridMenu: true,
        exporterCsvFilename: csvName || "asset_costs.csv"
    });
}

function setupYearFilter(costs, uiGridConstants) {
    const yearOptions = _.chain(costs)
        .map(c => c.cost.year)
        .uniq()
        .map(y => ({ label: y, value: y }))
        .value();

    return {
        type: uiGridConstants.filter.SELECT,
        selectOptions: yearOptions
    };
}


function setupOrgFilter(costs, uiGridConstants) {
    const orgOptions = _.chain(costs)
        .filter('orgUnit') // may be null
        .map(c => c.orgUnit.name)
        .uniq()
        .map(n => ({ label: n, value: n }))
        .value();

    return {
        type: uiGridConstants.filter.SELECT,
        selectOptions: orgOptions
    };
}


function controller(displayNameService, uiGridConstants, $scope, $animate) {

    const vm = this;

    const options = _.defaults(vm.options || {}, DEFAULT_OPTIONS);

    const colDefinitions = prepareColumns(displayNameService, uiGridConstants);
    const gridOptions = prepareGridOptions(colDefinitions, options, uiGridConstants, $animate);

    setupExportOptions(gridOptions, vm.csvName);

    /* setup year and org filters */
    const configureWithCosts = (costs) => {
        vm.gridOptions.data = costs;
        colDefinitions.yearCol = setupYearFilter(costs, uiGridConstants);
        colDefinitions.orgCol = setupOrgFilter(costs, uiGridConstants);
    };


    const filterAmount = ({ max, min }) => {
        const minFilter = colDefinitions.amountCol.filters[0];
        const maxFilter = colDefinitions.amountCol.filters[1];

        minFilter.term = min;
        if (max === Number.MAX_VALUE) {
            maxFilter.term = null;
        } else {
            maxFilter.term = max;
        }
    };


    const filterOrgUnit = ({ name }) => {
        colDefinitions.orgCol.filter.term = name;
    };


    const applyFilter = (filterOptions) => {
        if (! filterOptions) return;
        filterAmount(filterOptions);
    };


    $scope.$watch('ctrl.costs', configureWithCosts);
    $scope.$watch('ctrl.selectedBucket', applyFilter);

    vm.gridOptions = gridOptions;
}

controller.$inject = [
    'WaltzDisplayNameService',
    'uiGridConstants',
    '$scope',
    '$animate'
];


export default () => {
    return {
        restrict: 'E',
        replace: true,
        template: require('./asset-cost-table.html'),
        scope: {},
        bindToController: BINDINGS,
        controllerAs: 'ctrl',
        controller
    };
};

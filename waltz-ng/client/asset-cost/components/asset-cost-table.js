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
import {assetCostKindNames, toGridOptions} from "./../../common/services/display-names";
import {mkEntityLinkGridCell} from "../../common/link-utils";
import template from './asset-cost-table.html';
import {initialiseData} from "../../common/index";


const DEFAULT_OPTIONS = {
    showAssetCode: true,
    showAppName: false,
    showOrgUnit: false
};


const bindings = {
    costs: '<',
    options: '<?',
    selectedBucket: '<?',
    csvName: '@?'
};


const initialState = {

};


function prepareColumns(uiGridConstants) {
    const kindCol = {
        field: 'cost.kind',
        displayName: 'Cost Type',
        cellFilter: 'toDisplayName:"CostKind"',
        filter: {
            type: uiGridConstants.filter.SELECT,
            selectOptions: toGridOptions(assetCostKindNames)
        }
    };

    const amountCol = {
        field: 'cost.amount',
        displayName: 'Amount',
        cellClass: 'waltz-grid-currency',
        cellTemplate: `
            <div class="ui-grid-cell-contents">
                <waltz-currency-amount amount="COL_FIELD">
                </waltz-currency-amount>
            </div>`,
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

    const appCol = mkEntityLinkGridCell('Application', 'application', 'none');

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


function controller(uiGridConstants, $animate) {

    const vm = initialiseData(this, initialState);

    const options = _.defaults(vm.options || {}, DEFAULT_OPTIONS);

    const colDefinitions = prepareColumns(uiGridConstants);
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

    const applyFilter = (filterOptions) => {
        if (! filterOptions) return;
        filterAmount(filterOptions);
    };


    vm.$onChanges = () => {
        configureWithCosts(vm.costs);
        applyFilter(vm.filterOptions);
    };

    vm.gridOptions = gridOptions;
}


controller.$inject = [
    'uiGridConstants',
    '$animate'
];


const component = {
    controller,
    template,
    bindings
};


export default {
    component,
    id: 'waltzAssetCostTable'
};


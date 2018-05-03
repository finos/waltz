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
import {mkEntityLinkGridCell} from "../../common/grid-utils";
import {initialiseData} from "../../common/index";

import template from "./asset-cost-table.html";


const DEFAULT_OPTIONS = {
    showAssetCode: true,
    showAppName: false
};


const bindings = {
    costs: '<',
    options: '<?',
    csvName: '@?'
};


const initialState = {
    columnDefs: [],
    options: DEFAULT_OPTIONS
};


function prepareColumns(uiGridConstants, options) {
    const kindCol = {
        field: 'cost.costKind',
        displayName: 'Cost Type',
        cellFilter: 'toDisplayName:"CostKind"'
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

    const assetCodeCol = {
        field: 'assetCode'
    };

    return determineColumns({
        kindCol,
        amountCol,
        yearCol,
        appCol,
        assetCodeCol
    }, options);
}


function determineColumns(colDefinitions, options) {
    const cols = [
        options.showAssetCode ? colDefinitions.assetCodeCol : null,
        options.showAppName ? colDefinitions.appCol : null,
        colDefinitions.yearCol,
        colDefinitions.kindCol,
        colDefinitions.amountCol
    ];

    return _.compact(cols);
}


function controller(uiGridConstants) {

    const vm = initialiseData(this, initialState);

    vm.$onChanges = () => {
        if(vm.costs) {
            vm.columnDefs = prepareColumns(uiGridConstants, vm.options);
        }
    };

    vm.onGridInitialise = (opts) => {
        vm.gridExportFn = opts.exportFn;
    };

    vm.exportAssetCosts = () => {
        vm.gridExportFn(vm.csvName);
    };
}


controller.$inject = [
    'uiGridConstants'
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


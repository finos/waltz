/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017, 2018, 2019 Waltz open source project
 * See README.md for more information
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific
 *
 */

import _ from "lodash";
import {mkEntityLinkGridCell} from "../../../common/grid-utils";
import {initialiseData} from "../../../common/index";

import template from "./asset-cost-table.html";


const DEFAULT_OPTIONS = {
    showAssetCode: true,
    showAppName: false
};


const bindings = {
    costs: "<",
    options: "<?",
    csvName: "@?"
};


const initialState = {
    columnDefs: [],
    options: DEFAULT_OPTIONS
};


function prepareColumns(uiGridConstants, options) {
    const kindCol = {
        field: "cost.costKind",
        displayName: "Cost Type",
        cellFilter: "toDisplayName:'CostKind'"
    };

    const amountCol = {
        field: "cost.amount",
        displayName: "Amount",
        cellClass: "waltz-grid-currency",
        cellTemplate: `
            <div class="ui-grid-cell-contents">
                <waltz-currency-amount amount="COL_FIELD">
                </waltz-currency-amount>
            </div>`,
        filters: [
            {
                condition: uiGridConstants.filter.GREATER_THAN,
                placeholder: "Greater than"
            }, {
                condition: uiGridConstants.filter.LESS_THAN,
                placeholder: "Less than"
            }
        ]
    };

    const yearCol = {
        field: "cost.year",
        displayName: "Year"
    };

    const appCol = mkEntityLinkGridCell("Application", "application", "none", "right");

    const assetCodeCol = {
        field: "assetCode"
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
    "uiGridConstants"
];


const component = {
    controller,
    template,
    bindings
};


export default {
    component,
    id: "waltzAssetCostTable"
};


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

import template from "./report-grid-view-section.html";
import {initialiseData} from "../../../common";
import {mkSelectionOptions} from "../../../common/selector-utils";
import {CORE_API} from "../../../common/services/core-api-utils";
import _ from "lodash";
import ReportGridControlPanel from "../svelte/ReportGridControlPanel.svelte";
import {activeSummaries, columnDefs, filters, selectedGrid} from "../svelte/report-grid-store";
import {
    mkLocalStorageFilterKey,
    mkRowFilter,
    prepareColumnDefs,
    prepareTableData
} from "../svelte/report-grid-utils";
import {displayError} from "../../../common/error-utils";
import toasts from "../../../svelte-stores/toast-store";
import {coalesceFns} from "../../../common/function-utils";


const bindings = {
    parentEntityRef: "<",
};

const initData = {
    ReportGridControlPanel
};

const localStorageKey = "waltz-report-grid-view-section-last-id";

function controller($scope, serviceBroker, localStorageService) {

    const vm = initialiseData(this, initData);

    function refresh(filters = []) {

        const rowFilter = mkRowFilter(filters);

        const workingTableData = _.map(
            vm.allTableData,
            d => Object.assign({}, d, { visible: rowFilter(d) }));

        vm.tableData = _.filter(workingTableData, d => d.visible);
    }


    function getDefaultSummaryColumns(columnDefs) {
        const dfltFilters = _
            .chain(columnDefs)
            .filter(d => d.usageKind === "SUMMARY")
            .map(d => d.id)
            .value();

        if (!_.isEmpty(dfltFilters)) {
            toasts.info("Using default filters for grid");
        }

        return dfltFilters;
    }


    function getSummaryColumnsFromLocalStorage(gridData) {

        const key = mkLocalStorageFilterKey(gridData?.definition.id);
        const value = localStorage.getItem(key);

        try {
            return JSON.parse(value)
        } catch (e) {
            console.log("Cannot parse local storage value", { e, key, value });
            return [];
        }
    }


    function getSummaryColumns(gridData) {
        return coalesceFns(
            () => getSummaryColumnsFromLocalStorage(gridData),
            () => getDefaultSummaryColumns(gridData?.definition.columnDefinitions));
    }


    function loadGridData() {
        serviceBroker
            .loadViewData(
                CORE_API.ReportGridStore.getViewById,
                [vm.gridId, vm.selectionOptions], {force: true})
            .then(r => {

                const gridData = r.data;
                vm.loading = false;

                if (gridData) {
                    vm.rawGridData = gridData;

                    const summaries = getSummaryColumns(gridData);
                    activeSummaries.set(summaries);

                    selectedGrid.set(gridData);
                    columnDefs.set(gridData?.definition.columnDefinitions);

                    vm.allTableData = prepareTableData(vm.rawGridData);
                    vm.allColumnDefs = prepareColumnDefs(vm.rawGridData);
                    refresh();
                }
            })
            .catch(e => {
                displayError("Could not load grid data for id: " + vm.gridId, e)
                vm.loading = false;
            });
    }

    vm.$onChanges = () => {

        if (vm.parentEntityRef) {

            vm.selectionOptions = mkSelectionOptions(vm.parentEntityRef);
            const lastUsedGridId = localStorageService.get(localStorageKey);

            if (lastUsedGridId) {
                vm.gridId = lastUsedGridId;
                vm.loading = true;
                loadGridData();
            }

        }
    };

    vm.onGridSelect = (grid) => {
        if (!grid) {
            return;
        }
        $scope.$applyAsync(() => {
            localStorageService.set(localStorageKey, grid.id);
            vm.gridId = grid.id;
            loadGridData();
        });
    };

    vm.onUpdateColumns = () => {
        loadGridData();
    };

    filters.subscribe((f) => {
        $scope.$applyAsync(() => {
            if (vm.rawGridData) {
                refresh(f)
            }
        });
    })

    activeSummaries.subscribe((d) => {
        $scope.$applyAsync(() => {
            vm.summaryCols = d;
        });
    })

}

controller.$inject = ["$scope", "ServiceBroker", "localStorageService"];

const component = {
    controller,
    bindings,
    template
};

export default {
    id: "waltzReportGridViewSection",
    component,
}
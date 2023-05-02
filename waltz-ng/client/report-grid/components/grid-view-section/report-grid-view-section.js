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
import _ from "lodash";
import ReportGridControlPanel from "../svelte/ReportGridControlPanel.svelte";
import {combineColDefs, mkLocalStorageFilterKey, prepareColumnDefs} from "../svelte/report-grid-utils";
import {displayError} from "../../../common/error-utils";
import {coalesceFns} from "../../../common/function-utils";
import {gridService} from "../svelte/report-grid-service";
import {showGridSelector} from "../svelte/report-grid-ui-service";


const bindings = {
    parentEntityRef: "<",
    selectedGridId: "<?"
};

const initData = {
    ReportGridControlPanel,
    showGridSelector: true
};

const localStorageKey = "waltz-report-grid-view-section-last-id";

function controller($scope, serviceBroker, localStorageService) {

    const vm = initialiseData(this, initData);

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
            () => []);
    }


    function loadGridData(selectedGridId, selectionOptions) {
        vm.loading = true;
        gridService.selectGrid(selectedGridId, selectionOptions)
            .catch(e => displayError("Could not load grid data for id: " + vm.gridId, e))
            .finally(() => vm.loading = false);
    }

    const unsubTableData = gridService.tableData.subscribe((td) => {
        $scope.$applyAsync(
            () => {
                vm.tableData = _.filter(td, d => d.visible);
            })
    });

    const unsubColDefs = gridService.gridDefinition.subscribe((definition) => {
        $scope.$applyAsync(() => {
            vm.gridDefinition = definition;
            const cols = combineColDefs(definition);
            vm.allColumnDefs = prepareColumnDefs(cols);
        })
    });

    vm.$onDestroy = () => {
        unsubTableData();
        unsubColDefs();
    }

    vm.$onChanges = () => {

        if (vm.parentEntityRef) {

            vm.selectionOptions = mkSelectionOptions(vm.parentEntityRef);
            const lastUsedGridId = localStorageService.get(localStorageKey);

            showGridSelector.set(true);

            if (!_.isNil(vm.selectedGridId)) {
                loadGridData(vm.selectedGridId, vm.selectionOptions);
                showGridSelector.set(false);
            } else if (lastUsedGridId) {
                loadGridData(lastUsedGridId, vm.selectionOptions);
            }
        }
    };

    vm.onGridSelect = (grid) => {
        if (!grid) {
            return;
        }
        localStorageService.set(localStorageKey, grid.gridId);
        loadGridData(grid.gridId, vm.selectionOptions);
    };

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
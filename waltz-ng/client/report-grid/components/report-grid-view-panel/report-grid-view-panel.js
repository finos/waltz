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

import template from "./report-grid-view-panel.html";
import {initialiseData} from "../../../common";
import {mkSelectionOptions} from "../../../common/selector-utils";
import {CORE_API} from "../../../common/services/core-api-utils";
import _ from "lodash";
import ReportGridControlPanel from "../svelte/ReportGridControlPanel.svelte";
import {activeSummaryColRefs, filters, selectedGrid, columnDefs} from "../svelte/report-grid-store";
import {mkPropNameForRef, mkRowFilter, prepareColumnDefs, prepareTableData} from "../svelte/report-grid-utils";
import {displayError} from "../../../common/error-utils";

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

        vm.columnDefs = _.map(vm.allColumnDefs, cd => {
            if (cd.allowSummary){
                return Object.assign(cd, { menuItems: [
                    {
                        title: "Add to summary",
                        icon: "ui-grid-icon-info-circled",
                        action: function() {
                            vm.onAddSummary(cd);
                        },
                        context: vm
                    }
                ]})
            } else {
                return cd;
            }
        });

        const rowFilter = mkRowFilter(filters);

        const workingTableData =  _.map(
            vm.allTableData,
            d => Object.assign({}, d, { visible: rowFilter(d) }));

        vm.tableData = _.filter(workingTableData, d => d.visible);
    }

    function loadGridData() {
        serviceBroker
            .loadViewData(
                CORE_API.ReportGridStore.getViewById,
                [vm.gridId, mkSelectionOptions(vm.parentEntityRef)], {force: true})
            .then(r => {
                vm.loading = false;
                vm.rawGridData = r.data;

                selectedGrid.set(r.data);
                columnDefs.set(r.data.definition.columnDefinitions);

                vm.allTableData = prepareTableData(vm.rawGridData);
                vm.allColumnDefs = prepareColumnDefs(vm.rawGridData);
                refresh();
            })
            .catch(e => {
                displayError("Could not load grid data for id: " + vm.gridId, e)
            });
    }

    vm.$onChanges = () => {

        const lastUsedGridId = localStorageService.get(localStorageKey);

        if (! vm.parentEntityRef) return;

        if (lastUsedGridId) {
            vm.gridId = lastUsedGridId;
            vm.selectionOptions = mkSelectionOptions(vm.parentEntityRef);
            vm.loading = true;
            loadGridData();
        }
    };

    vm.onGridSelect = (grid) => {
        $scope.$applyAsync(() => {
            localStorageService.set(localStorageKey, grid.id);
            vm.gridId = grid.id;
            loadGridData();
        });
    };

    vm.onAddSummary = (c) => {
        const colRef = mkPropNameForRef(c.columnDef.columnEntityReference);
        const newActiveList = _.concat(vm.summaryCols, [colRef]);
        activeSummaryColRefs.set(newActiveList);
    };

    vm.onUpdateColumns = () => {
        loadGridData();
    };

    filters.subscribe((f) => {
        $scope.$applyAsync(() => {
            if(vm.rawGridData){
                refresh(f)
            }
        });
    })

    activeSummaryColRefs.subscribe((d) => {
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
    id: "waltzReportGridViewPanel",
    component,
}
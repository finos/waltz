/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017, 2018, 2019 Waltz open source project
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
import {initialiseData, isEmpty} from "../../../common";
import {mkLinkGridCell} from "../../../common/grid-utils";
import {mkApplicationSelectionOptions} from "../../../common/selector-utils";
import {CORE_API} from "../../../common/services/core-api-utils";
import {entityLifecycleStatus} from '../../../common/services/enums/entity-lifecycle-status';

import template from "./complexity-section.html";


const bindings = {
    filters: "<",
    parentEntityRef: "<",
    csvName: "@?"
};


const initialState = {
    csvName: "complexity.csv",
    gridData: [],
    summarySelection: null,
    visibility: {
        summary: true,
        detail: false
    }
};


function mkGridData(complexity = [], apps = []) {
    const appsByIds = _.keyBy(apps, "id");

    return _.chain(complexity)
        .map(c => Object.assign(c, { app: appsByIds[c.id] }))
        .value();
}



function controller($q, serviceBroker) {

    const vm = initialiseData(this, initialState);

    const loadAll = () => {
        vm.selector = mkApplicationSelectionOptions(
            vm.parentEntityRef,
            undefined,
            [entityLifecycleStatus.ACTIVE.key],
            vm.filters);

        const appPromise = serviceBroker
            .loadViewData(
                CORE_API.ApplicationStore.findBySelector,
                [ vm.selector ])
            .then(r => vm.apps = r.data);

        const complexityPromise = serviceBroker
            .loadViewData(
                CORE_API.ComplexityStore.findBySelector,
                [ vm.selector ])
            .then(r => vm.complexity = r.data);

        return $q.all([appPromise, complexityPromise]);
    };

    vm.$onInit = () => {
        loadAll();
    };


    vm.$onChanges = (changes) => {
        if (! vm.parentEntityRef) {
            return;
        }

        if (changes.filters) {
            loadAll()
                .then(() => {
                    if(vm.visibility.detail === true) {
                        vm.gridData = mkGridData(vm.complexity, vm.apps);
                    }
                });
        }
    };

    vm.onSummarySelect = (d) => vm.summarySelection = d;

    vm.showSummary = () => {
        vm.visibility.summary = true;
        vm.visibility.detail = false;
    };

    vm.showDetail = () => {
        vm.visibility.summary = false;
        vm.visibility.detail = true;

        vm.columnDefs = [
            Object.assign(
                mkLinkGridCell("Application", "app.name", "app.id", "main.app.view"),
                { sort: { direction: "asc" }, width: "30%" }
            ),
            { field: "app.assetCode", displayName: "Asset Code"},
            { field: "connectionComplexity.score", displayName: "Connection Score", cellFilter: "toFixed:'2'" },
            { field: "measurableComplexity.score", displayName: "Viewpoints Score", cellFilter: "toFixed:'2'" },
            { field: "serverComplexity.score", displayName: "Server Score", cellFilter: "toFixed:'2'" },
            { field: "overallScore", displayName: "Overall Score", cellFilter: "toFixed:'2'" }
        ];

        if (isEmpty(vm.gridData)) {
            vm.gridData = mkGridData(vm.complexity, vm.apps);
        }
    };
}


controller.$inject = [
    "$q",
    "ServiceBroker"
];


export const component = {
    template,
    bindings,
    controller
};

export const id = "waltzComplexitySection";


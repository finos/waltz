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
import {initialiseData, isEmpty} from "../../../common";
import {mkLinkGridCell} from "../../../common/grid-utils";
import {CORE_API} from "../../../common/services/core-api-utils";
import template from "./complexity-section.html";
import {mkSelectionOptions} from "../../../common/selector-utils";

const bindings = {
    parentEntityRef: '<',
    csvName: '@?'
};


const initialState = {
    csvName: 'complexity.csv',
    gridData: [],
    gridDataCount: 0,
    summarySelection: null,
    visibility: {
        summary: true,
        detail: false
    },

    exportGrid: () => {}
};


function mkGridData(complexity = [], apps = []) {
    const appsByIds = _.keyBy(apps, 'id');

    return _.chain(complexity)
        .map(c => Object.assign(c, { app: appsByIds[c.id] }))
        .value();
}



function controller(serviceBroker) {

    const vm = initialiseData(this, initialState);

    vm.$onInit = () => {
    };

    vm.$onChanges = () => {
        if (! vm.parentEntityRef) return;
        const selector = mkSelectionOptions(vm.parentEntityRef);

        serviceBroker
            .loadViewData(
                CORE_API.ApplicationStore.findBySelector,
                [ selector ])
            .then(r => vm.apps = r.data);

        serviceBroker
            .loadViewData(
                CORE_API.ComplexityStore.findBySelector,
                [ selector ])
            .then(r => vm.complexity = r.data);
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
                mkLinkGridCell('Application', 'app.name', 'app.id', 'main.app.view'),
                { sort: { direction: 'asc' }, width: '30%' }
            ),
            { field: 'connectionComplexity.score', displayName: 'Connection Score', cellFilter: "toFixed:'2'" },
            { field: 'measurableComplexity.score', displayName: 'Viewpoints Score', cellFilter: "toFixed:'2'" },
            { field: 'serverComplexity.score', displayName: 'Server Score', cellFilter: "toFixed:'2'" },
            { field: 'overallScore', displayName: 'Overall Score', cellFilter: "toFixed:'2'" }
        ];

        vm.onGridInitialise = (e) => {
            vm.exportGrid = () => e.exportFn(vm.csvName);
        };

        vm.onGridChange = (e) => {
            vm.gridDataCount = e.entriesCount;
        };

        if (isEmpty(vm.gridData)) {
            vm.gridData = mkGridData(vm.complexity, vm.apps);
        }
    };
}


controller.$inject = [
    'ServiceBroker'
];


export const component = {
    template,
    bindings,
    controller
};

export const id = 'waltzComplexitySection';


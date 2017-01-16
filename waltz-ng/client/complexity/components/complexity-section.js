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
import {initialiseData, isEmpty, mkLinkGridCell} from "../../common";

const bindings = {
    complexity: '<',
    apps: '<',
    loadAll: '<',
    csvName: '@'
};


const initialState = {
    summarySelection: null,
    gridData: [],
    gridDataCount: 0,
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


function controller() {

    const vm = initialiseData(this, initialState);

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
            { field: 'measurableComplexity.score', displayName: 'Characteristic Score', cellFilter: "toFixed:'2'" },
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
];


const component = {
    template: require('./complexity-section.html'),
    bindings,
    controller
};


export default component;
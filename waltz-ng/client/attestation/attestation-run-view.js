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
import moment from "moment";

import {CORE_API} from "../common/services/core-api-utils";
import {formats, initialiseData} from "../common/index";
import {mkEntityLinkGridCell} from "../common/grid-utils";
import {amber, green, red} from "../common/colors";

import template from "./attestation-run-view.html";


const initialState = {
    run: null,
    recipients: null,
    instances: [],
    selectedInstance: null,
    columnDefs: [],
    tableData: [],
    onGridInitialise: (cfg) => console.log('default grid initialise handler for attestation-run-view')
};


function mkRagRating(run = {}, instance = {}) {
    if(instance.attestedBy) {
        return { rag: 'G', name: 'Complete', color: green }
    }
    const dueDate = moment.utc(run.dueDate, formats.parse );
    const now = moment.utc();

    if(now > dueDate) {
        return { rag: 'R', name: 'Overdue', color: red };
    } else {
        return { rag: 'A', name: 'Pending', color: amber };
    }
}


function mkInstancesWithRagRating(run = {}, instances = []) {
   return _.map(instances, i => Object.assign({}, i, { rating: mkRagRating(run, i) }));
}

const ratingOrdinal = {
    'R': 3,
    'A': 2,
    'G': 1,
    'Z': 0
};


const ratingCellTemplate = `
    <div class="ui-grid-cell-contents">
        <waltz-rating-indicator-cell rating="COL_FIELD">
        </waltz-rating-indicator-cell>
    </div>`;


function prepareColumnDefs() {
    const initialCols = [
        mkEntityLinkGridCell('Subject', 'parentEntity'),
        {
            field: 'rating',
            name: 'Status',
            cellTemplate: ratingCellTemplate,
            sortingAlgorithm: (a, b) => {
                if(a.rag == b.rag) return 0;
                return ratingOrdinal[a.rag] - ratingOrdinal[b.rag];
            }
        },
        {
            field: 'attestedBy',
            name: 'Attested By',
            cellTemplate: '<div class="ui-grid-cell-contents"><a ui-sref="main.profile.view ({userId: COL_FIELD})"><span ng-bind="COL_FIELD"></span></a></div>'
        },
        {
            field: 'attestedAt',
            name: 'Attested At',
            cellTemplate: '<div class="ui-grid-cell-contents"><waltz-from-now timestamp="COL_FIELD"></waltz-from-now></div>'
        },
        {
            name: 'Recipients',
            cellTemplate: '<div class="ui-grid-cell-contents"><a ng-click="grid.appScope.selectInstance(row.entity)" class="clickable">Show</a></div>'
        }
    ];

    return initialCols;
}


function controller($q,
                    $stateParams,
                    serviceBroker) {

    const vm = initialiseData(this, initialState);
    const id = $stateParams.id;

    vm.columnDefs = prepareColumnDefs();

    const loadData = () => {
        $q.all([
            serviceBroker.loadViewData(CORE_API.AttestationRunStore.getById, [id]),
            serviceBroker.loadViewData(CORE_API.AttestationInstanceStore.findByRunId, [id])
        ]).then(([runResult, instancesResult]) => {
            vm.run = runResult.data;
            vm.instances = mkInstancesWithRagRating(vm.run, instancesResult.data);
            vm.tableData = vm.instances;

        });
    };

    loadData();

    vm.selectInstance = (instance) => {
      vm.selectedInstance = instance;
    };

    vm.onGridInitialise = (cfg) => {
        vm.exportData = () => cfg.exportFn("attestation_instances.csv");
    }

}


controller.$inject = [
    '$q',
    '$stateParams',
    'ServiceBroker'
];


export default {
    template,
    controller,
    controllerAs: 'ctrl'
};
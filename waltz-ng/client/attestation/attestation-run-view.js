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
    instanceColumnDefs: [],
    instanceTableData: [],
    reminderLink: ""
};


function mkRagRating(run = {}, instance = {}) {
    if(instance.attestedBy) {
        return { rag: "G", name: "Complete", color: green }
    }
    const dueDate = moment.utc(run.dueDate, formats.parse );
    const now = moment.utc();

    if(now > dueDate) {
        return { rag: "R", name: "Overdue", color: red };
    } else {
        return { rag: "A", name: "Pending", color: amber };
    }
}


function mkInstancesWithRagRating(run = {}, instances = []) {
    return _.map(instances, i => Object.assign({}, i, { rating: mkRagRating(run, i) }));
}

const ratingOrdinal = {
    "R": 3,
    "A": 2,
    "G": 1,
    "Z": 0
};


const ratingCellTemplate = `
    <div class="ui-grid-cell-contents">
        <waltz-rating-indicator-cell rating="COL_FIELD">
        </waltz-rating-indicator-cell>
    </div>`;


function prepareInstanceColumnDefs() {
    return [
        mkEntityLinkGridCell("Subject", "parentEntity"),
        {
            field: "rating",
            name: "Status",
            cellTemplate: ratingCellTemplate,
            sortingAlgorithm: (a, b) => {
                if(a.rag === b.rag) return 0;
                return ratingOrdinal[a.rag] - ratingOrdinal[b.rag];
            }
        },
        {
            field: "attestedBy",
            name: "Attested By",
            cellTemplate: "<div class=\"ui-grid-cell-contents\"><a ui-sref=\"main.profile.view ({userId: COL_FIELD})\"><span ng-bind=\"COL_FIELD\"></span></a></div>"
        },
        {
            field: "attestedAt",
            name: "Attested At",
            cellTemplate: "<div class=\"ui-grid-cell-contents\"><waltz-from-now timestamp=\"COL_FIELD\"></waltz-from-now></div>"
        },
        {
            name: "Recipients",
            cellTemplate: "<div class=\"ui-grid-cell-contents\"><a ng-click=\"grid.appScope.selectInstance(row.entity)\" class=\"clickable\">Show</a></div>"
        }
    ];
}


function controller($q,
                    $stateParams,
                    serviceBroker) {

    const vm = initialiseData(this, initialState);
    const id = $stateParams.id;

    vm.instanceColumnDefs = prepareInstanceColumnDefs();

    const loadData = () => {
        $q.all([
            serviceBroker.loadViewData(CORE_API.AttestationRunStore.getById, [id]),
            serviceBroker.loadViewData(CORE_API.AttestationInstanceStore.findByRunId, [id]),
            serviceBroker.loadViewData(CORE_API.AttestationRunStore.findRecipientsByRunId, [id])
        ]).then(([runResult, instancesResult, recipientsResult]) => {
            vm.run = runResult.data;
            vm.instances = mkInstancesWithRagRating(vm.run, instancesResult.data);
            vm.instanceTableData = vm.instances;

            const recipients = _
                .chain(recipientsResult.data)
                .filter(d => d.pendingCount > 0)
                .map(d => d.userId)
                .join(",")
                .value();

            vm.reminderLink = `mailto:?bcc=${recipients}&subject=Attestation%20Reminder&body=Please%20complete%20your%20attestation%20for%20${vm.run.name}`;
        });
    };

    loadData();

    vm.selectInstance = (instance) => {
        vm.selectedInstance = instance;
    };

}


controller.$inject = [
    "$q",
    "$stateParams",
    "ServiceBroker"
];


export default {
    template,
    controller,
    controllerAs: "ctrl"
};
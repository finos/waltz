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
import {initialiseData} from "../common/index";
import {mkEntityLinkGridCell, mkLinkGridCell} from "../common/grid-utils";

import template from "./survey-instance-list-user-view.html";
import roles from "../user/system-roles";


const initialState = {
    incompleteColumnDefs: [],
    completeColumnDefs: [],
    surveyInstancesAndRuns: [],
    showSurveyTemplateButton: false
};


function mkSurveyData(surveyRuns = [], surveyInstances = []) {
    const runsById = _.keyBy(surveyRuns, "id");

    const mappedData = _.map(surveyInstances, instance => {
        return {
            "surveyInstance": instance,
            "surveyRun": runsById[instance.surveyRunId]
        }
    });

    const [incomplete = [], complete = []] = _.partition(
        mappedData,
        data => data.surveyInstance.status === "NOT_STARTED"
            || data.surveyInstance.status === "IN_PROGRESS"
            || data.surveyInstance.status === "REJECTED");

    return {
        "incomplete": incomplete,
        "complete": complete
    };
}


function mkCommonColumnDefs() {
    return [
        {
            field: "surveyInstance.id",
            name: "ID",
            width: "5%"
        },
        mkEntityLinkGridCell("Subject", "surveyInstance.surveyEntity"),
        {
            field: "surveyInstance.surveyEntityExternalId",
            name: "Subject External Id",
            width: "10%"
        },
        {
            field: "surveyInstance.status",
            name: "Status",
            cellFilter: "toDisplayName:'surveyInstanceStatus'",
            width: "10%"
        },
        {
            field: "surveyRun.issuedOn",
            name: "Issued",
            cellTemplate: "<div class=\"ui-grid-cell-contents\"><waltz-from-now timestamp=\"COL_FIELD\" days-only=\"true\"></waltz-from-now></div>",
            width: "10%"
        },
        {
            field: "surveyInstance.dueDate",
            name: "Due",
            cellTemplate: "<div class=\"ui-grid-cell-contents\"><waltz-from-now timestamp=\"COL_FIELD\" days-only=\"true\"></waltz-from-now></div>",
            width: "10%"
        },
    ];
}


function mkIncompleteColumnDefs() {
    const columnDefs = mkCommonColumnDefs();
    columnDefs.splice(0, 0, Object.assign(
        {},
        mkLinkGridCell("Survey", "surveyRun.name", "surveyInstance.id", "main.survey.instance.response.edit"),
        { width: "25%"}
    ));
    return columnDefs;
}


function mkCompleteColumnDefs() {
    const columnDefs = mkCommonColumnDefs();
    columnDefs.splice(0, 0, Object.assign(
        {},
        mkLinkGridCell("Survey", "surveyRun.name", "surveyInstance.id", "main.survey.instance.response.view"),
        { width: "25%"}
    ));

    const approved = {
        field: "surveyInstance.approvedBy",
        name: "Approved By",
        cellTemplate: `<div class="ui-grid-cell-contents">
                               <span ng-if="row.entity.surveyInstance.approvedBy">
                                    <span ng-bind="row.entity.surveyInstance.approvedBy">
                                    </span>,
                                </span>
                                <waltz-from-now class='text-muted'
                                            ng-if="row.entity.surveyInstance.approvedAt"
                                            timestamp="row.entity.surveyInstance.approvedAt">
                                </waltz-from-now>
                                <span ng-if="! row.entity.surveyInstance.approvedAt">
                                    -
                                </span>
                           </div>`,
        width: "15%"
    };

    columnDefs.push(approved);

    return columnDefs;
}


function controller($q,
                    surveyInstanceStore,
                    surveyRunStore,
                    userService) {

    const vm = initialiseData(this, initialState);

    vm.incompleteColumnDefs = mkIncompleteColumnDefs();
    vm.completeColumnDefs = mkCompleteColumnDefs();

    userService.whoami()
        .then(user => vm.user = user)
        .then(() => vm.showSurveyTemplateButton = userService.hasRole(vm.user, roles.SURVEY_ADMIN)
            || userService.hasRole(vm.user, roles.SURVEY_TEMPLATE_ADMIN));

    const surveyRunsPromise = surveyRunStore.findForUser();
    const surveyInstancesPromise = surveyInstanceStore.findForUser();

    $q.all([surveyRunsPromise, surveyInstancesPromise])
        .then(([surveyRuns, surveyInstances]) => {
            vm.surveys = mkSurveyData(surveyRuns, surveyInstances);
        });
}


controller.$inject = [
    "$q",
    "SurveyInstanceStore",
    "SurveyRunStore",
    "UserService"
];


export default {
    template,
    controller,
    controllerAs: "ctrl"
};


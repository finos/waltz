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

import template from "./survey-run-view.html";
import {timeFormat} from "d3-time-format";
import {initialiseData} from "../common";
import {mkEntityLabelGridCell} from "../common/grid-utils";

const columnDefs = [
    mkEntityLabelGridCell("Entity", "surveyEntity", "left", "right"),
    {
        field: "id",
        name: "Actions",
        cellTemplate: `
            <div class="ui-grid-cell-contents">
                <a class='clickable'
                   ui-sref="main.survey.instance.response.view ({id: COL_FIELD })">
                    Show survey
                </a>
            </div>`
    }, {
        field: "status",
        cellFilter: "toDisplayName:'surveyInstanceStatus'"
    }, {
        field: "dueDate",
        cellTemplate: `
            <div class="ui-grid-cell-contents">
                <waltz-from-now timestamp="COL_FIELD"
                                days-only="true">
                </waltz-from-now>
            </div>`
    }, {
        field: "submittedBy",
    }, {
        field: "approvedBy",
    }
];


const initialState = {
    columnDefs
};


function controller($stateParams,
                    notification,
                    surveyInstanceStore,
                    surveyRunStore,
                    surveyTemplateStore) {

    const id = $stateParams.id;
    const vm = initialiseData(this, initialState);

    const loadSurveyRun = () => surveyRunStore
        .getById(id)
        .then(sr => {
            vm.surveyRun = sr;
            return surveyTemplateStore
                .getById(sr.surveyTemplateId);
        })
        .then(t => vm.surveyTemplate = t);

    const loadInstances = () => {
        surveyInstanceStore
            .findForSurveyRun(id)
            .then(xs => vm.surveyInstances = _.sortBy(xs, d => _.toLower(d.surveyEntity.name)));
    };

    loadSurveyRun();
    loadInstances();

    vm.updateDueDate = (newVal) => {
        if (!newVal) {
            notification.error("Due date cannot be blank");
        } else {
            if (confirm("This will update the due date of all the instances under this run. " +
                    "Are you sure you want to continue?")) {
                surveyRunStore.updateDueDate(id, {
                    newDateVal: timeFormat("%Y-%m-%d")(newVal)
                })
                    .then(r => {
                        notification.success("Survey run due date updated successfully");
                        loadSurveyRun();
                        loadInstances();
                    },
                        r => notification.error("Failed to update survey run due date")
                    );
            }
        }
    };
}

controller.$inject = [
    "$stateParams",
    "Notification",
    "SurveyInstanceStore",
    "SurveyRunStore",
    "SurveyTemplateStore",
];


const page = {
    template,
    controller,
    controllerAs: "ctrl"
};


export default page;
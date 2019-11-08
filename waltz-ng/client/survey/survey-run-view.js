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

import template from "./survey-run-view.html";
import {timeFormat} from "d3-time-format";
import {initialiseData} from "../common";
import {mkEntityLinkGridCell} from "../common/grid-utils";

const columnDefs = [
    mkEntityLinkGridCell("Entity", "surveyEntity", "left", "right"),
    {
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
    }, {
        field: "id",
        name: "Actions",
        cellTemplate: `
            <div class="ui-grid-cell-contents">
                <a class='clickable'
                   ui-sref="main.survey.instance.response.view ({id: COL_FIELD })">
                    Show survey
                </a> 
            </div>`
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
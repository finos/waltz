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
import {mkLinkGridCell} from "../common/grid-utils";

import template from "./survey-template-view.html";
import {CORE_API} from "../common/services/core-api-utils";
import {displayError} from "../common/error-utils";


const initialState = {
    template: {},
    columnDefs: [],
    issuedAndCompletedRuns: [],
    issuedAndCompletedRunsEnriched: [],
    draftRuns: [],
    questionInfos: [],
    runCompletionRates: {}
};


function mkColumnDefs() {
    return [
        Object.assign({},
            mkLinkGridCell("Name", "surveyRun.name", "surveyRun.id", "main.survey.run.view"),
            { width: "25%"}
        ),
        {
            field: "surveyRun.status",
            name: "Status",
            cellFilter: "toDisplayName:'surveyRunStatus'",
            width: "7%"
        },
        {
            field: "completionRateStats",
            name: "Responses",
            cellTemplate: `<div uib-popover="{{[COL_FIELD].popoverText}}"
                                popover-trigger="mouseenter"
                                popover-append-to-body="true">
                            <uib-progress max="COL_FIELD.totalCount"
                                          animate="false"
                                          class="waltz-survey-progress">
                                <uib-bar value="COL_FIELD.completedCount"
                                         ng-bind="COL_FIELD.completedCount"
                                         type="success">
                                </uib-bar>
                                <uib-bar value="COL_FIELD.inProgressCount"
                                         ng-bind="COL_FIELD.inProgressCount"
                                         type="info">
                                </uib-bar>
                                <uib-bar value="COL_FIELD.notStartedCount"
                                         ng-bind="COL_FIELD.notStartedCount"
                                         type="warning">
                                </uib-bar>
                            </uib-progress>
                        </div>`,
            width: "15%"
        },
        {
            field: "surveyRun.contactEmail",
            name: "Contact",
            width: "20%"
        },
        {
            field: "surveyRun.issuedOn",
            name: "Issued",
            cellTemplate: "<div class=\"ui-grid-cell-contents\"><waltz-from-now timestamp=\"COL_FIELD\" days-only=\"true\"></waltz-from-now></div>",
            width: "8%"
        },
        {
            field: "surveyRun.dueDate",
            name: "Due",
            cellTemplate: "<div class=\"ui-grid-cell-contents\"><waltz-from-now timestamp=\"COL_FIELD\" days-only=\"true\"></waltz-from-now></div>",
            width: "7%"
        },
        {
            field: "owner.displayName",
            name: "Owner",
            width: "10%",
            cellTemplate: "<div class=\"ui-grid-cell-contents\"><span ng-bind=\"COL_FIELD\"></span></div>",
        },
        {
            field: "",
            name: "Actions",
            width: "8%",
            cellTemplate:
                `
                <div class="ui-grid-cell-contents">
                    <a ng-click="grid.appScope.deleteRun(row.entity.surveyRun)"
                       ng-if="row.entity.isRunOwnedByLoggedInUser"
                       uib-popover="Delete this Survey Run"
                       popover-placement="left"
                       popover-trigger="mouseenter" 
                       class="btn btn-xs btn-danger waltz-visibility-child-30">
                        <waltz-icon name="trash-o"></waltz-icon>
                    </a>
                </div>
                `
        }
    ];
}


function computePopoverTextForStats(surveyRun, stats) {
    return surveyRun.status === "COMPLETED"
        ? `${stats.completedCount} completed, ${stats.expiredCount} expired`
        : `${stats.completedCount} completed, ${stats.inProgressCount} in progress, ${stats.notStartedCount} not started`;
}

function controller($q,
                    $state,
                    $stateParams,
                    notification,
                    serviceBroker) {
    const vm = initialiseData(this, initialState);

    vm.columnDefs = mkColumnDefs();

    const templateId = $stateParams.id;

    // template
    serviceBroker
        .loadViewData(CORE_API.SurveyTemplateStore.getById, [ templateId ])
        .then(r => {
            const template= r.data;
            if (template) {
                vm.template = template;
                serviceBroker
                    .loadViewData(CORE_API.PersonStore.getById, [ template.ownerId ])
                    .then(r => vm.owner = r.data);
            }
        });

    // runs
    const loadRuns = () => {
        let userPromise = serviceBroker
            .loadAppData(CORE_API.UserStore.whoami)
            .then(r => r.data);

        let runsPromise = serviceBroker
            .loadViewData(CORE_API.SurveyRunStore.findByTemplateId, [templateId], { force: true })
            .then(r => r.data);

        $q.all([userPromise, runsPromise])
            .then(([user, runsData]) => {
                [vm.issuedAndCompleted, vm.draft] = _
                    .chain(runsData)
                    .map(d => {
                        const stats = d.completionRateStats;
                        stats.popoverText = computePopoverTextForStats(d.surveyRun, stats);
                        d.isRunOwnedByLoggedInUser = d.owner
                            ? (_.toLower(d.owner.email) === _.toLower(user.userName))
                            : false;
                        return d;
                    })
                    .partition(d => d.surveyRun.status !== "DRAFT")
                    .value();
            });
    };

    loadRuns();

    // questions
    serviceBroker
        .loadViewData(CORE_API.SurveyQuestionStore.findForTemplate, [templateId])
        .then(r => vm.questionInfos = r.data);

    const updateTemplateStatus = (newStatus, successMessage) => {
        serviceBroker
            .execute(
                CORE_API.SurveyTemplateStore.updateStatus,
                [ templateId, { newStatus }])
            .then(r => {
                vm.template.status = newStatus;
                notification.success(successMessage);
            })
            .catch(e => {
                displayError(notification, `Could not update status to ${newStatus}`, e);
            });
    };

    vm.markTemplateAsActive = () => {
        updateTemplateStatus("ACTIVE", "Survey template successfully marked as Active");
    };

    vm.markTemplateAsDraft = () => {
        if (confirm("Existing survey responses might become incompatible if questions are modified. " +
                    "Are you sure you want to mark this template as draft?")) {
            updateTemplateStatus("DRAFT", "Survey template successfully marked as Draft");
        }
    };

    vm.markTemplateAsObsolete = () => {
        if (confirm("Are you sure you wish to mark this template as obsolete ?")) {
            updateTemplateStatus("OBSOLETE", "Survey template successfully marked as Obsolete");
        }
    };

    vm.cloneTemplate = () => {
        if (confirm("Are you sure you want to clone this template?")) {
            serviceBroker
                .execute(
                    CORE_API.SurveyTemplateStore.clone,
                    [ templateId ])
                .then(r => {
                    notification.success("Survey template cloned successfully");
                    $state.go("main.survey.template.view", {id: r.data});
                });
        }
    };

    vm.deleteRun = (surveyRun) => {
        if (confirm(`Are you sure you want to delete this survey run: ${surveyRun.name}? Any responses collected so far will also be deleted.`)) {
            serviceBroker
                .execute(
                    CORE_API.SurveyRunStore.deleteById,
                    [ surveyRun.id ])
                .then(() => {
                    notification.warning("Survey run deleted");
                    loadRuns();
                })
                .catch(e => {
                    displayError(notification, "Survey run could not be deleted", e);
                });
        }
    };
}


controller.$inject = [
    "$q",
    "$state",
    "$stateParams",
    "Notification",
    "ServiceBroker"
];


const page = {
    controller,
    controllerAs: "ctrl",
    template
};


export default page;
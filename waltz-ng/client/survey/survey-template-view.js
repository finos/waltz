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
import toasts from "../svelte-stores/toast-store";
import SystemRoles from "../user/system-roles";

const initialState = {
    template: {},
    columnDefs: [],
    issuedAndCompletedRuns: [],
    issuedAndCompletedRunsEnriched: [],
    draftRuns: [],
    questions: [],
    runCompletionRates: {},
    canCreateRun: false

};


function mkColumnDefs() {
    return [
        Object.assign(
            {},
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
                <div class="ui-grid-cell-contents" style="padding: 2px; display: flex; gap: 5px">
                    <button ng-click="grid.appScope.withdrawOpenSurveys(row.entity.surveyRun)"
                            ng-disabled="!row.entity.hasOpenSurveys"
                            ng-if="row.entity.isRunOwnedByLoggedInUser || row.entity.hasAdminRights"
                            title="Withdraw all open instances ('Not Started' and 'In Progress')"
                            class="btn btn-xs btn-warning waltz-visibility-child-30 small">
                        <waltz-icon name="wpforms"></waltz-icon>
                    </button>
                    <button ng-click="grid.appScope.deleteRun(row.entity.surveyRun)"
                           ng-if="row.entity.isRunOwnedByLoggedInUser || row.entity.isTemplateOwnerOrAdmin"
                           title="Delete this Survey Run"
                           class="btn btn-xs btn-danger waltz-visibility-child-30">
                            <waltz-icon name="trash-o"></waltz-icon>
                    </button>
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
                    serviceBroker) {
    const vm = initialiseData(this, initialState);

    vm.columnDefs = mkColumnDefs();

    const templateId = $stateParams.id;

    // template
    const templatePromise = serviceBroker
        .loadViewData(CORE_API.SurveyTemplateStore.getById, [ templateId ])
        .then(r => vm.template = r.data);

    const ownerPromise = templatePromise
        .then(template => serviceBroker
            .loadViewData(CORE_API.PersonStore.getById, [ template.ownerId ])
            .then(r => vm.owner = r.data));

    // runs
    const loadRuns = () => {
        let userPromise = serviceBroker
            .loadAppData(CORE_API.UserStore.whoami)
            .then(r => r.data);

        let runsPromise = serviceBroker
            .loadViewData(CORE_API.SurveyRunStore.findByTemplateId, [templateId], { force: true })
            .then(r => r.data);

        $q.all([userPromise, runsPromise, templatePromise, ownerPromise])
            .then(([user, runsData, template, owner]) => {

                const userIsAdmin = _.includes(user.roles, SystemRoles.ADMIN.key);
                const userHasRunAdmin = _.includes(user.roles, SystemRoles.SURVEY_ADMIN.key)
                const userHasIssuanceRole = _.isNil(template.issuanceRole) || _.includes(user.roles, template.issuanceRole);
                const userIsTemplateOwner = user.userName === owner.email;
                const userHasIssuanceRights = userHasIssuanceRole && userHasRunAdmin;
                const templateIsActive = template.status === "ACTIVE";

                vm.canCreateRun =  templateIsActive && ( userIsAdmin || userHasIssuanceRights);
                vm.isOwnerOrAdmin = userIsTemplateOwner || userIsAdmin;

                [vm.issuedAndCompleted, vm.draft] = _
                    .chain(runsData)
                    .map(d => {
                        const stats = d.completionRateStats;
                        stats.popoverText = computePopoverTextForStats(d.surveyRun, stats);
                        d.isRunOwnedByLoggedInUser = d.owner
                            ? (_.toLower(d.owner.email) === _.toLower(user.userName))
                            : false;
                        d.hasAdminRights = userIsTemplateOwner || userIsAdmin || userHasIssuanceRights
                        d.hasOpenSurveys = stats.inProgressCount > 0 || stats.notStartedCount > 0
                        return d;
                    })
                    .partition(d => d.surveyRun.status !== "DRAFT")
                    .value();
            });
    };

    loadRuns();

    // questions
    serviceBroker
        .loadViewData(CORE_API.SurveyQuestionStore.findQuestionsForTemplate, [templateId])
        .then(r => vm.questions = r.data);

    const updateTemplateStatus = (newStatus, successMessage) => {
        serviceBroker
            .execute(
                CORE_API.SurveyTemplateStore.updateStatus,
                [ templateId, { newStatus }])
            .then(() => {
                vm.template.status = newStatus;
                toasts.success(successMessage);
            })
            .catch(e => {
                displayError(`Could not update status to ${newStatus}`, e);
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

    vm.noRuns = () =>  _.isEmpty(vm.issuedAndCompleted)  && _.isEmpty(vm.draft);


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
                    toasts.success("Survey template cloned successfully");
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
                    toasts.warning("Survey run deleted");
                    loadRuns();
                })
                .catch(e => {
                    displayError("Survey run could not be deleted", e);
                });
        }
    };


    vm.removeTemplate = () => {
        if(confirm("Are you sure you want to delete this survey template? Any questions will also be deleted.")){
            serviceBroker
                .execute(CORE_API.SurveyTemplateStore.remove, [vm.template.id])
                .then(() => {
                    toasts.warning("Survey template deleted");
                    $state.go("main.survey.template.list");
                })
                .catch(e => {
                    displayError("Survey template could not be deleted", e);
                });
        }
    }

    vm.withdrawOpenSurveys = (surveyRun) => {
        console.log({surveyRun});
        if(confirm(`Are you sure you want withdraw all open surveys for this survey run: ${surveyRun.name}?`)){
            serviceBroker
                .execute(CORE_API.SurveyInstanceStore.withdrawOpenSurveysForRun, [surveyRun.id])
                .then(() => {
                    toasts.warning("Not started and In progress survey runs have been withdrawn");
                    loadRuns();
                })
                .catch(e => {
                    displayError("Could not withdraw open survey runs", e);
                });
        }
    }
}


controller.$inject = [
    "$q",
    "$state",
    "$stateParams",
    "ServiceBroker"
];


const page = {
    controller,
    controllerAs: "ctrl",
    template
};


export default page;
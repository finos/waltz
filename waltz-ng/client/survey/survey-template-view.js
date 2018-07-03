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
import {initialiseData} from "../common/index";
import {mkLinkGridCell} from "../common/grid-utils";

import template from "./survey-template-view.html";


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
            mkLinkGridCell('Name', 'name', 'id', 'main.survey.run.view'),
            { width: '25%'}
        ),
        {
            field: 'status',
            name: 'Status',
            cellFilter: "toDisplayName:'surveyRunStatus'",
            width: '5%'
        },
        {
            field: 'id',
            name: 'Responses',
            cellTemplate: `<div uib-popover="{{grid.appScope.runCompletionRates[COL_FIELD].text}}"
                                popover-trigger="mouseenter"
                                popover-append-to-body="true">
                            <uib-progress max="grid.appScope.runCompletionRates[COL_FIELD].totalCount"
                                          animate="false"
                                          class="waltz-survey-progress">
                                <uib-bar value="grid.appScope.runCompletionRates[COL_FIELD].completedCount"
                                         ng-bind="grid.appScope.runCompletionRates[COL_FIELD].completedCount"
                                         type="success">
                                </uib-bar>
                                <uib-bar value="grid.appScope.runCompletionRates[COL_FIELD].inProgressCount"
                                         ng-bind="grid.appScope.runCompletionRates[COL_FIELD].inProgressCount"
                                         type="info">
                                </uib-bar>
                                <uib-bar value="grid.appScope.runCompletionRates[COL_FIELD].notStartedCount"
                                         ng-bind="grid.appScope.runCompletionRates[COL_FIELD].notStartedCount"
                                         type="warning">
                                </uib-bar>
                                <uib-bar value="grid.appScope.runCompletionRates[COL_FIELD].expiredCount"
                                         ng-bind="grid.appScope.runCompletionRates[COL_FIELD].expiredCount"
                                         type="danger">
                                </uib-bar>
                            </uib-progress>
                        </div>`,
            width: '10%'
        },
        {
            field: 'contactEmail',
            name: 'Contact',
        },
        {
            field: 'issuedOn',
            name: 'Issued',
            cellTemplate: '<div class="ui-grid-cell-contents"><waltz-from-now timestamp="COL_FIELD" days-only="true"></waltz-from-now></div>',
            width: '10%'
        },
        {
            field: 'dueDate',
            name: 'Due',
            cellTemplate: '<div class="ui-grid-cell-contents"><waltz-from-now timestamp="COL_FIELD" days-only="true"></waltz-from-now></div>',
            width: '10%'
        },
        {
            field: 'owner.displayName',
            name: 'Owner',
            cellTemplate: '<div class="ui-grid-cell-contents"><span ng-bind="COL_FIELD"></span></div>',
        },
    ];
}


function controller($q,
                    $state,
                    $stateParams,
                    notification,
                    personStore,
                    surveyQuestionStore,
                    surveyRunStore,
                    surveyTemplateStore) {
    const vm = initialiseData(this, initialState);

    vm.columnDefs = mkColumnDefs();

    const templateId = $stateParams.id;

    vm.people = {};

    surveyTemplateStore
        .getById(templateId)
        .then(t => {
            if (t) {
                vm.template = t;
                personStore
                    .getById(t.ownerId)
                    .then(p => {
                        if (p) {
                            vm.people[t.ownerId] = p;
                        }
                    });
            }
        });

    surveyRunStore
        .findByTemplateId(templateId)
        .then(rs => {
            [vm.issuedAndCompletedRuns = [], vm.draftRuns = []] = _.partition(rs, r => r.status !== 'DRAFT');

            // populate completion rates
            vm.issuedAndCompletedRuns
                .forEach(run => {
                    surveyRunStore.getCompletionRate(run.id)
                        .then(rate => vm.runCompletionRates[run.id] = Object.assign({},
                            rate,
                            {'totalCount': rate.notStartedCount + rate.inProgressCount + rate.completedCount + rate.expiredCount},
                            {'text': (run.status === 'COMPLETED')
                                ? `${rate.completedCount} completed, ${rate.expiredCount} expired`
                                : `${rate.completedCount} completed, ${rate.inProgressCount} in progress, ${rate.notStartedCount} not started`})
                        )
                });

            // populate owners
            const personPromises = _.chain(vm.issuedAndCompletedRuns)
                .map('ownerId')
                .uniq()
                .map(personId => personStore
                    .getById(personId)
                    .then(p => {
                        if (p) {
                            vm.people[p.id] = p;
                        }
                        return p;
                    }))
                .value();

            $q.all(personPromises)
                .then(() => {
                    vm.issuedAndCompletedRunsEnriched = _.map(
                            vm.issuedAndCompletedRuns,
                            r => Object.assign(r, {owner: vm.people[r.ownerId]})
                    );
                });

        });

    surveyQuestionStore
        .findForTemplate(templateId)
        .then(qis => vm.questionInfos = qis);

    vm.markTemplateAsActive = () => {
        surveyTemplateStore.updateStatus(templateId, {
            newStatus: 'ACTIVE'
        })
        .then(updateCount => {
            vm.template.status = 'ACTIVE';
            notification.success('Survey template successfully marked as Active');
        })
    };

    vm.markTemplateAsDraft = () => {
        if (confirm('Existing survey responses might become incompatible if questions are modified. ' +
                    'Are you sure you want to mark this template as draft?')) {
            surveyTemplateStore.updateStatus(templateId, {
                newStatus: 'DRAFT'
            })
            .then(updateCount => {
                vm.template.status = 'DRAFT';
                notification.success('Survey template successfully marked as Draft');
            });
        }
    };

    vm.markTemplateAsObsolete = () => {
        surveyTemplateStore.updateStatus(templateId, {
            newStatus: 'OBSOLETE'
        })
        .then(updateCount => {
            vm.template.status = 'OBSOLETE';
            notification.success('Survey template successfully marked as Obsolete');
        });
    };

    vm.cloneTemplate = () => {
        if (confirm('Are you sure you want to clone this template?')) {
            surveyTemplateStore.clone(templateId)
                .then(newTemplateId => {
                    notification.success('Survey template cloned successfully');
                    $state.go('main.survey.template.view', {id: newTemplateId});
                });
        }
    };
}


controller.$inject = [
    '$q',
    '$state',
    '$stateParams',
    'Notification',
    'PersonStore',
    'SurveyQuestionStore',
    'SurveyRunStore',
    'SurveyTemplateStore'
];


const page = {
    controller,
    controllerAs: 'ctrl',
    template
};


export default page;
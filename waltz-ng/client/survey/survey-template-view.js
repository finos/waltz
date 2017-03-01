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

import _ from 'lodash';
import {initialiseData} from "../common/index";


const initialState = {
    template: {},
    issuedAndCompletedRuns: [],
    draftRuns: [],
    runCompletionRates: {}
};


function controller($stateParams,
                    personStore,
                    surveyRunStore,
                    surveyTemplateStore) {
    const vm = initialiseData(this, initialState);

    const templateId = $stateParams.id;

    surveyTemplateStore
        .getById(templateId)
        .then(t => vm.template = t)
        .then(t => personStore.getById(t.ownerId));

    vm.people = {};
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
            _.chain(vm.issuedAndCompletedRuns)
                .map('ownerId')
                .uniq()
                .value()
                .forEach(personId => personStore
                    .getById(personId)
                    .then(p => {
                        if (p) {
                            vm.people[p.id] = p;
                        }
                    }));
        });
}


controller.$inject = [
    '$stateParams',
    'PersonStore',
    'SurveyRunStore',
    'SurveyTemplateStore'
];


const page = {
    controller,
    controllerAs: 'ctrl',
    template: require('./survey-template-view.html')
};


export default page;
import {surveyTemplateResolver} from "./resolvers";
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

const surveyRunBaseState = {};


const surveyRunCreateState = {
    url: 'survey/template/{id:int}/new-run',
    views: {'content@': require('./survey-run-create')},
    resolve: {
        surveyTemplate: surveyTemplateResolver
    }
};


const surveyInstanceBaseState = {};


const surveyInstanceUserState = {
    url: 'survey/instance/user',
    views: {'content@': require('./survey-instance-list-user-view')}
};


const surveyInstanceResponseState = {
    url: 'survey/instance/{id:int}/response'
};


const surveyInstanceResponseEditState = {
    url: 'survey/instance/{id:int}/response/edit'
};


function setup($stateProvider) {
    $stateProvider
        .state('main.survey-run', surveyRunBaseState)
        .state('main.survey-run.create', surveyRunCreateState);

    $stateProvider
        .state('main.survey-instance', surveyInstanceBaseState)
        .state('main.survey-instance.user', surveyInstanceUserState)
        .state('main.survey-instance.response', surveyInstanceResponseState)
        .state('main.survey-instance.response-edit', surveyInstanceResponseEditState)
}


setup.$inject = ['$stateProvider'];


export default setup;
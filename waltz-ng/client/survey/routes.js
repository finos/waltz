import {surveyInstanceResolver, surveyTemplateResolver} from "./resolvers";
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

const baseState = {
    url: 'survey'
};


const runBaseState = {
    url: '/run'
};


const instanceBaseState = {
    url: '/instance'
};


const runCreateState = {
    url: '/template/{id:int}/new-run',
    views: {'content@': require('./survey-run-create')},
    resolve: {
        surveyTemplate: surveyTemplateResolver
    }
};


const instanceUserState = {
    url: '/user',
    views: {'content@': require('./survey-instance-list-user-view')}
};


const instanceViewState = {
    url: '/{id:int}/view',
    views: {'content@': require('./survey-instance-response-view')}
};


const instanceEditState = {
    url: '/{id:int}/edit',
    views: {'content@': require('./survey-instance-response-edit')}
};


function setup($stateProvider) {
    $stateProvider
        .state('main.survey', baseState)
        .state('main.survey.run', runBaseState)
        .state('main.survey.run.create', runCreateState)
        .state('main.survey.instance', instanceBaseState)
        .state('main.survey.instance.user', instanceUserState)
        .state('main.survey.instance.edit', instanceEditState)
        .state('main.survey.instance.view', instanceViewState)
        ;
}


setup.$inject = ['$stateProvider'];


export default setup;
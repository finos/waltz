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
import SurveyRunCreate from './survey-run-create';
import SurveyRunView from './survey-run-view';
import SurveyInstanceListUserView from './survey-instance-list-user-view';
import SurveyInstanceView from './survey-instance-view';
import SurveyInstanceResponseView from './survey-instance-response-view';
import SurveyInstanceResponseEdit from './survey-instance-response-edit';
import SurveyTemplateList from './survey-template-list';
import SurveyTemplateCreate from './survey-template-create';
import SurveyTemplateEdit from './survey-template-edit';
import SurveyTemplateView from './survey-template-view';


const baseState = {
    url: 'survey'
};


const runBaseState = {
    url: '/run'
};


const templateBaseState = {
    url: '/template'
};


const instanceBaseState = {
    url: '/instance'
};


const instanceResponseBaseState = {
    url: '/{id:int}/response'
};


const runCreateState = {
    url: '/template/{id:int}/new-run',
    views: {'content@': SurveyRunCreate}
};


const runViewState = {
    url: '/{id:int}',
    views: {'content@': SurveyRunView}
};


const instanceUserState = {
    url: '/user',
    views: {'content@': SurveyInstanceListUserView}
};


const instanceViewState = {
    url: '/{id:int}/view',
    views: {'content@': SurveyInstanceView}
};


const instanceResponseViewState = {
    url: '/view',
    views: {'content@': SurveyInstanceResponseView}
};


const instanceResponseEditState = {
    url: '/edit',
    views: {'content@': SurveyInstanceResponseEdit}
};


const templateListState = {
    url: '/list',
    views: {'content@': SurveyTemplateList}
};


const templateCreateState = {
    url: '/create',
    views: {'content@': SurveyTemplateCreate}
};


const templateEditState = {
    url: '/{id:int}/edit',
    views: {'content@': SurveyTemplateEdit}
};


const templateViewState = {
    url: '/{id:int}/view',
    views: {'content@': SurveyTemplateView}
};


function setup($stateProvider) {
    $stateProvider
        .state('main.survey', baseState)
        .state('main.survey.run', runBaseState)
        .state('main.survey.run.create', runCreateState)
        .state('main.survey.run.view', runViewState)
        .state('main.survey.instance', instanceBaseState)
        .state('main.survey.instance.user', instanceUserState)
        .state('main.survey.instance.view', instanceViewState)
        .state('main.survey.instance.response', instanceResponseBaseState)
        .state('main.survey.instance.response.edit', instanceResponseEditState)
        .state('main.survey.instance.response.view', instanceResponseViewState)
        .state('main.survey.template', templateBaseState)
        .state('main.survey.template.list', templateListState)
        .state('main.survey.template.create', templateCreateState)
        .state('main.survey.template.edit', templateEditState)
        .state('main.survey.template.view', templateViewState);

}


setup.$inject = ['$stateProvider'];


export default setup;
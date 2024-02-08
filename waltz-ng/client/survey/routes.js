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
import SurveyRunCreate from './survey-run-create';
import SurveyRunView from './survey-run-view';
import SurveyInstanceListUserView from './survey-instance-list-user-view';
import SurveyInstanceResponseView from './survey-instance-response-view';
import SurveyInstanceResponseEdit from './survey-instance-response-edit';
import SurveyTemplateList from './survey-template-list';
import SurveyTemplateCreate from './survey-template-create';
import SurveyTemplateImport from './survey-template-import';
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
    url: '/template/{id:int}/run-create',
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


// TODO: this state is now simply an alias for the instanceResponseViewState, delete in 1.20
const instanceViewState = {
    url: '/{id:int}/view',
    views: {'content@': SurveyInstanceResponseView}
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


const templateImportState = {
    url: '/import',
    views: {"content@": SurveyTemplateImport.id}
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
        .state('main.survey.template.import', templateImportState)
        .state('main.survey.template.edit', templateEditState)
        .state('main.survey.template.view', templateViewState);

}


setup.$inject = ['$stateProvider'];


export default setup;
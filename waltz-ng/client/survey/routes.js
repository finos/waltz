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

const baseState = {};

const createState = {
    url: 'survey/template/{id:int}/new-run',
    views: {'content@': require('./survey-run-create')},
    resolve: {
        surveyTemplate: surveyTemplateResolver
    }
};

const responseState = {
    url: 'survey/run/{id:int}/response'
};

function setup($stateProvider) {
    $stateProvider
        .state('main.survey-run', baseState)
        .state('main.survey-run.create', createState)
        .state('main.survey-run.response', responseState);

}


setup.$inject = ['$stateProvider'];


export default setup;
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
import {initialiseData} from "../common/index";
import template from './survey-template-create.html';


const initialState = {
    surveyTemplate: {},
    targetEntityKinds: [{
        name: 'Application',
        value: 'APPLICATION'
    },{
        name: 'Change Initiative',
        value: 'CHANGE_INITIATIVE'
    }]
};


function controller($state,
                    notification,
                    surveyTemplateStore) {

    const vm = initialiseData(this, initialState);

    vm.onSubmit = () => {
        surveyTemplateStore
            .create(vm.surveyTemplate)
            .then(templateId => {
                notification.success('Survey template created successfully');
                $state.go('main.survey.template.edit', {id: templateId});
            }, () => {
                notification.error('Failed to create survey template, ensure that the template name is unique');
            });
    }
}


controller.$inject = [
    '$state',
    'Notification',
    'SurveyTemplateStore'
];


const page = {
    controller,
    controllerAs: 'ctrl',
    template
};


export default page;
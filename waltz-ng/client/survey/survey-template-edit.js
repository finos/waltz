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
import template from './survey-template-edit.html';


const initialState = {
    editingQuestion: false,
    questionFieldTypes: [{
        name: 'Text',
        value: 'TEXT'
    },{
        name: 'Text Area',
        value: 'TEXTAREA'
    },{
        name: 'Number',
        value: 'NUMBER'
    },{
        name: 'Boolean',
        value: 'BOOLEAN'
    },{
        name: 'Date',
        value: 'DATE'
    },{
        name: 'Dropdown',
        value: 'DROPDOWN'
    },{
        name: 'Application',
        value: 'APPLICATION'
    },{
        name: 'Person',
        value: 'PERSON'
    }],
    selectedQuestionInfo: {},
    surveyQuestionInfos: [],
    surveyTemplate: {},
    targetEntityKinds: [{
        name: 'Application',
        value: 'APPLICATION'
    },{
        name: 'Change Initiative',
        value: 'CHANGE_INITIATIVE'
    }]
};


function controller($stateParams,
                    notification,
                    surveyQuestionStore,
                    surveyTemplateStore) {

    const vm = initialiseData(this, initialState);
    vm.id = $stateParams.id;

    surveyTemplateStore
        .getById(vm.id)
        .then(template => vm.surveyTemplate = template);

    const loadQuestions = () =>
        surveyQuestionStore
            .findForTemplate(vm.id)
            .then(qis => vm.surveyQuestionInfos = qis);

    vm.updateTemplate = () => {
        surveyTemplateStore
            .update({
                id: vm.surveyTemplate.id,
                name: vm.surveyTemplate.name,
                description: vm.surveyTemplate.description,
                targetEntityKind: vm.surveyTemplate.targetEntityKind
            })
            .then(updateCount => notification.success('Survey template updated successfully'));
    };

    vm.showAddQuestionForm = () => {
        vm.editingQuestion = true;
        const currentMaxPos = _.chain(vm.surveyQuestionInfos)
            .map(qi => qi.question.position)
            .max()
            .value();

        vm.selectedQuestionInfo = {
            question: {
                surveyTemplateId: vm.id,
                isMandatory: false,
                allowComment: false,
                position: (currentMaxPos || 0) + 10
            },
            dropdownEntries: []
        };
    };

    vm.showEditQuestionForm = (qi) => {
        vm.editingQuestion = true;
        vm.selectedQuestionInfo = _.cloneDeep(qi);
    };


    vm.cancelQuestionForm = () => {
        vm.editingQuestion = false;
        vm.selectedQuestionInfo = null;
    };

    vm.createQuestion = (qi) => {
        surveyQuestionStore
            .create(qi)
            .then(() => {
                notification.success('Survey question created successfully');
                loadQuestions();
                vm.cancelQuestionForm();
            });
    };

    vm.updateQuestion = (qi) => {
        surveyQuestionStore
            .update(qi)
            .then(() => {
                notification.success('Survey question updated successfully');
                loadQuestions();
                vm.cancelQuestionForm();
            });
    };

    vm.deleteQuestion = (qi) => {
        if (confirm("Are you sure you want to delete this question?")) {
            surveyQuestionStore
                .deleteQuestion(qi.question.id)
                .then(() => {
                    notification.success('Survey question deleted successfully');
                    loadQuestions();
                    vm.cancelQuestionForm();
                });
        }
    };

    vm.updateEntries = (entries) => {
        vm.selectedQuestionInfo.dropdownEntries = _.map(
            entries,
            (e, i) => Object.assign({
                questionId: vm.selectedQuestionInfo.question.id,
                position: i + 1
            }, e));
    };

    loadQuestions();
}


controller.$inject = [
    '$stateParams',
    'Notification',
    'SurveyQuestionStore',
    'SurveyTemplateStore'
];


const page = {
    controller,
    controllerAs: 'ctrl',
    template
};


export default page;
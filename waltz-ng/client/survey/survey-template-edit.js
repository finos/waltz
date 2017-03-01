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
import _ from "lodash";
import {initialiseData} from "../common/index";


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
    }],
    selectedQuestion: {},
    surveyQuestions: [],
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

    surveyQuestionStore
        .findForTemplate(vm.id)
        .then(questions => vm.surveyQuestions = questions);

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
        vm.selectedQuestion = {
            surveyTemplateId: vm.id,
            position: vm.surveyQuestions.length + 1
        };
    };

    vm.showEditQuestionForm = (q) => {
        vm.editingQuestion = true;
        vm.selectedQuestion = q;
    };


    vm.cancelQuestionForm = () => {
        vm.editingQuestion = false;
        vm.selectedQuestion = null;
    };

    vm.createQuestion = (q) => {
        surveyQuestionStore
            .create(q)
            .then(questionId => {
                notification.success('Survey question created successfully');
                q.id = questionId;
                vm.surveyQuestions.push(q);
                vm.cancelQuestionForm();
            });
    };

    vm.updateQuestion = (q) => {
        surveyQuestionStore
            .update(q)
            .then(updateCount => {
                notification.success('Survey question updated successfully');
                vm.cancelQuestionForm();
            });
    };

    vm.deleteQuestion = (q) => {
        if (confirm("Are you sure you want to delete this question?")) {
            surveyQuestionStore
                .deleteQuestion(q.id)
                .then(deleteCount => {
                    notification.success('Survey question deleted successfully');
                    _.remove(vm.surveyQuestions, que => que.id === q.id);
                    vm.cancelQuestionForm();
                });
        }
    };

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
    template: require('./survey-template-edit.html')
};


export default page;
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

import _ from "lodash";
import {initialiseData} from "../common/index";
import template from "./survey-template-edit.html";
import {displayError} from "../common/error-utils";
import {CORE_API} from "../common/services/core-api-utils";
import toasts from "../svelte-stores/toast-store";

/*
    Note: this list of functions/operators is derived from the capabilities of BigEval and the extension methods
    in `surveyUtils::mkSurveyExpressionEvaluator`
*/
const qInclusionPredicateHelp = `
The inclusion predicate allows for questions to be conditionally included in a survey depending on the values of other fields.
See the documentation for a complete list of functions and their arguments.  Below is a selection of the main functions/operators:

* \`< <= > >= == != && || ! \`: logical operators
* \`isChecked(extId, <defaultValue>)\`: \`true\` if the question with the given ext id is checked, \`false\` if not checked,
  or \`defaultValue\` if the answer is currently undefined.
* \`numberValue(extId, <defaultValue>)\`: numeric value of the response for the given ext id (or \`defaultValue\`)
* \`ditto(extId)\`: evaluates same conditions from a different question.  Useful for repetition of complex predicates.
* \`val(extId, <defaultValue>)\`: returns the current value
* \`assessmentRating(name|extId, <defaultValue>)\`: returns code value of the matching rating (returns null if no default given and no assessment found)
* \`belongsToOrgUnit(name|extId)\`: returns true if the subject app is part of the given org unit tree
* \`dataTypeUsages(name|extId)\`: returns set of usage kinds for the given data types (use the \`=~\` operator to test for membership)
* \`isRetiring()\`: (application only) true if app has planned retirement date but no actual retirement date
* \`hasDataType(name|extId)\`: returns whether the specified datatype (or a descendent) is in use by the app
* \`hasInvolvement(roleName)\`: returns whether the subject entity has any involvement record with the given role name
* \`hasLifecyclePhase(lifecyclePhase)\`: returns true of the given lifecycle phase matches the entities lifecycles phase. (PRODUCTION, DEVELOPMENT, CONCEPTUAL, RETIRED)
* \`isAppKind(applicationKind)\`: returns true of the given app kind phase matches the application kind. ( IN&#95;HOUSE, INTERNALLY&#95;HOSTED, EXTERNALLY&#95;HOSTED, EUC, THIRD&#95;PARTY, CUSTOMISED, EXTERNAL)

See the <a href="https://commons.apache.org/proper/commons-jexl/reference/syntax.html" target="_blank">JEXL documentation</a>.
`;


const initialState = {
    editingQuestion: false,
    questionFieldTypes: [{
        name: "Text",
        value: "TEXT"
    },{
        name: "Text Area",
        value: "TEXTAREA"
    },{
        name: "Number",
        value: "NUMBER"
    },{
        name: "Boolean",
        value: "BOOLEAN"
    },{
        name: "Date",
        value: "DATE"
    },{
        name: "Dropdown",
        value: "DROPDOWN"
    },{
        name: "Dropdown (Multi-Select)",
        value: "DROPDOWN_MULTI_SELECT"
    },{
        name: "Measurable tree (Multi-Select)",
        value: "MEASURABLE_MULTI_SELECT"
    },{
        name: "Application",
        value: "APPLICATION"
    },{
        name: "Person",
        value: "PERSON"
    }],
    selectedQuestionInfo: {},
    surveyQuestions: [],
    surveyTemplate: {},
    targetEntityKinds: [{
        name: "Application",
        value: "APPLICATION"
    },{
        name: "Change Initiative",
        value: "CHANGE_INITIATIVE"
    }],
    qInclusionPredicateHelp
};


function controller($q,
                    $stateParams,
                    serviceBroker) {

    const vm = initialiseData(this, initialState);
    vm.id = $stateParams.id;

    serviceBroker
        .loadAppData(CORE_API.MeasurableCategoryStore.findAll)
        .then(r => vm.measurableCategories = r.data);

    serviceBroker
        .loadViewData(CORE_API.SurveyTemplateStore.getById, [vm.id])
        .then(r => vm.surveyTemplate = r.data);

    const loadQuestions = () => {
        const questionPromise = serviceBroker
            .loadViewData(CORE_API.SurveyQuestionStore.findQuestionsForTemplate, [vm.id], {force: true})
            .then(r => vm.surveyQuestions = r.data);

        const dropdownPromise = serviceBroker
            .loadViewData(CORE_API.SurveyQuestionStore.findDropdownEntriesForTemplate, [vm.id], {force: true})
            .then(r => vm.dropdownEntriesByQuestionId = _.groupBy(r.data, d => d.questionId));

        return $q
            .all([questionPromise, dropdownPromise]);
    }

    vm.updateTemplate = () => {
        serviceBroker
            .execute(
                CORE_API.SurveyTemplateStore.update,
                [{
                    id: vm.surveyTemplate.id,
                    name: vm.surveyTemplate.name,
                    description: vm.surveyTemplate.description,
                    targetEntityKind: vm.surveyTemplate.targetEntityKind,
                    externalId: vm.surveyTemplate.externalId
                }])
            .then(() => toasts.success("Survey template updated successfully"));
    };

    vm.showAddQuestionForm = () => {
        vm.editingQuestion = true;
        const currentMaxPos = _
            .chain(vm.surveyQuestionInfos)
            .map(qi => qi.question.position)
            .max()
            .value();

        vm.selectedQuestionInfo = {
            question: {
                surveyTemplateId: vm.id,
                isMandatory: false,
                allowComment: false,
                position: (currentMaxPos || 0) + 10,
                externalId: null,
                parentExternalId: null,
                label: null,
                inclusionPredicate: null
            },
            dropdownEntries: []
        };
    };

    vm.showEditQuestionForm = (question) => {
        vm.editingQuestion = true;
        const dropdownEntries = vm.dropdownEntriesByQuestionId[question.id] || [];
        vm.selectedQuestionInfo = {
            question: _.cloneDeep(question),
            dropdownEntries: _.cloneDeep(dropdownEntries)
        };
    };


    vm.cancelQuestionForm = () => {
        vm.editingQuestion = false;
        vm.selectedQuestionInfo = null;
    };

    vm.createQuestion = (qi) => {

        if(qi.question.fieldType === "MEASURABLE_MULTI_SELECT"){
            qi.question.qualifierEntity.kind = "MEASURABLE_CATEGORY";
        }

        serviceBroker
            .execute(CORE_API.SurveyQuestionStore.create, [qi])
            .then(() => {
                toasts.success("Survey question created successfully");
                loadQuestions();
                vm.cancelQuestionForm();
            });
    };

    vm.updateQuestion = (qi) => {

        if(qi.question.fieldType === "MEASURABLE_MULTI_SELECT"){
            qi.question.qualifierEntity.kind = "MEASURABLE_CATEGORY";
        }

        serviceBroker
            .execute(CORE_API.SurveyQuestionStore.update, [qi])
            .then(() => {
                toasts.success("Survey question updated successfully");
                loadQuestions();
                vm.cancelQuestionForm();
            });
    };

    vm.deleteQuestion = (qi) => {
        if (confirm("Are you sure you want to delete this question?")) {
            serviceBroker
                .execute(CORE_API.SurveyQuestionStore.deleteQuestion, [qi.question.id])
                .then(() => {
                    toasts.success("Survey question deleted successfully");
                    loadQuestions();
                    vm.cancelQuestionForm();
                })
                .catch(e => displayError("Survey question was not deleted", e))
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
    "$q",
    "$stateParams",
    "ServiceBroker",
];


const page = {
    controller,
    controllerAs: "ctrl",
    template
};


export default page;

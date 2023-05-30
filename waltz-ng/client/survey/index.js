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
import angular from "angular";
import {registerComponents, registerStores} from "../common/module-utils";

import SurveyInstanceStore from "./services/survey-instance-store";
import SurveyInstanceViewStore from "./services/survey-instance-view-store";
import SurveyRunStore from "./services/survey-run-store";
import SurveyTemplateStore from "./services/survey-template-store";

import SurveyInstanceList from "./components/instance-list/survey-instance-list";
import SurveyInstanceSummary from "./components/instance-summary/survey-instance-summary";
import surveyQuestionMeasurableRenderer
    from "./components/survey-question-measurable-renderer/survey-question-measurable-renderer"
import SurveyResponseInput from "./components/input/survey-response-input"
import SurveyResponseAnswer from "./components/answer/survey-response-answer"

import surveySection from "./components/survey-section";
import measurableMultiSelect from "./components/measurable-multi-select/survey-measurable-multi-select"
import Routes from "./routes";
import SurveyDropdownEditor from "./components/dropdown/survey-dropdown-editor";
import SurveyRunCreateGeneral from "./components/survey-run-create-general";
import SurveyRunCreateRecipient from "./components/survey-run-create-recipient";
import SurveyRunOverview from "./components/survey-run-overview";
import SurveyTemplateOverview from "./components/survey-template-overview";
import SurveyTemplateQuestionOverviewTable
    from "./components/question-overview-table/survey-template-question-overview-table";
import SurveyQuestionStore from "./services/survey-question-store";
import StringListInput from "./components/string-list-input/string-list-input";

export default () => {
    const module = angular.module("waltz.survey", []);

    module
        .config(Routes);

    registerComponents(module, [
        surveySection,
        measurableMultiSelect,
        surveyQuestionMeasurableRenderer,
        StringListInput
    ]);

    module
        .component("waltzSurveyDropdownEditor", SurveyDropdownEditor)
        .component("waltzSurveyRunCreateGeneral", SurveyRunCreateGeneral)
        .component("waltzSurveyRunCreateRecipient", SurveyRunCreateRecipient)
        .component("waltzSurveyRunOverview", SurveyRunOverview)
        .component("waltzSurveyTemplateOverview", SurveyTemplateOverview);



    registerStores(module, [
        SurveyInstanceStore,
        SurveyInstanceViewStore,
        SurveyQuestionStore,
        SurveyRunStore,
        SurveyTemplateStore
    ]);

    registerComponents(module, [
        SurveyInstanceList,
        SurveyInstanceSummary,
        SurveyTemplateQuestionOverviewTable,
        SurveyResponseAnswer,
        SurveyResponseInput
    ]);

    return module.name;
};

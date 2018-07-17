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
import angular from "angular";
import {registerComponents, registerStores} from "../common/module-utils";

import SurveyInstanceStore from "./services/survey-instance-store";
import SurveyRunStore from "./services/survey-run-store";
import SurveyTemplateStore from "./services/survey-template-store";
import SurveyInstanceList from "./components/instance-list/survey-instance-list";

import surveySection from "./components/survey-section";
import Routes from './routes';
import SurveyDropdownEditor from './components/dropdown/survey-dropdown-editor';
import SurveyRunCreateGeneral from './components/survey-run-create-general';
import SurveyRunCreateRecipient from './components/survey-run-create-recipient';
import SurveyRunOverview from './components/survey-run-overview';
import SurveyTemplateOverview from './components/survey-template-overview';
import SurveyQuestionStore from './services/survey-question-store';

export default () => {
    const module = angular.module('waltz.survey', []);

    module
        .config(Routes);

    registerComponents(module, [
        surveySection
    ]);

    module
        .component('waltzSurveyDropdownEditor', SurveyDropdownEditor)
        .component('waltzSurveyRunCreateGeneral', SurveyRunCreateGeneral)
        .component('waltzSurveyRunCreateRecipient', SurveyRunCreateRecipient)
        .component('waltzSurveyRunOverview', SurveyRunOverview)
        .component('waltzSurveyTemplateOverview', SurveyTemplateOverview);


    module
        .service('SurveyQuestionStore', SurveyQuestionStore);

    registerStores(module, [
        SurveyInstanceStore,
        SurveyRunStore,
        SurveyTemplateStore
    ]);

    registerComponents(module, [
        SurveyInstanceList
    ]);

    return module.name;
};

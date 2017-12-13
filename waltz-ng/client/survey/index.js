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

import surveyInstanceStore from './services/survey-instance-store';
import surveyRunStore from './services/survey-run-store';

import surveySection from './components/survey-section';

export default () => {
    const module = angular.module('waltz.survey', []);

    module
        .config(require('./routes'));

    registerComponents(module, [
        surveySection
    ]);

    module
        .component('waltzSurveyDropdownEditor', require('./components/dropdown/survey-dropdown-editor'))
        .component('waltzSurveyRunCreateGeneral', require('./components/survey-run-create-general'))
        .component('waltzSurveyRunCreateRecipient', require('./components/survey-run-create-recipient'))
        .component('waltzSurveyRunOverview', require('./components/survey-run-overview'))
        .component('waltzSurveyTemplateOverview', require('./components/survey-template-overview'));

    registerStores(module, [
        surveyInstanceStore,
        surveyRunStore
    ]);

    module
        .service('SurveyTemplateStore', require('./services/survey-template-store'))
        .service('SurveyQuestionStore', require('./services/survey-question-store'));

    return module.name;
};

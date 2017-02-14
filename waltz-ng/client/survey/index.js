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
import angular from "angular";

export default () => {
    const module = angular.module('waltz.survey', []);

    module
        .config(require('./routes'));

    module
        .component('waltzSurveyRunCreateGeneral', require('./components/survey-run-create-general'))
        .component('waltzSurveyRunCreateRecipient', require('./components/survey-run-create-recipient'))
        .component('waltzSurveyInstanceList', require('./components/survey-instance-list'));

    module
        .service('SurveyInstanceStore', require('./services/survey-instance-store'))
        .service('SurveyRunStore', require('./services/survey-run-store'))
        .service('SurveyTemplateStore', require('./services/survey-template-store'))
        .service('SurveyQuestionStore', require('./services/survey-question-store'));

    return module.name;
};

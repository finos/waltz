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

import template from "./survey-template-question-overview-table.html";
import {initialiseData} from "../../../common";


const bindings = {
    questionInfos: "<",
    actions: "<?"
}

const modes = {
    DEFAULT_VIEW: "DEFAULT_VIEW",
    CONDITIONAL_VIEW: "CONDITIONAL_VIEW",
};


const initialState = {
    actions: [],
    modes,
    mode: modes.DEFAULT_VIEW
};


function controller($transclude) {
    const vm = initialiseData(this, initialState);
}


controller.$inject = ["$transclude"];


export default {
    id: "waltzSurveyTemplateQuestionOverviewTable",
    component: {
        template,
        controller,
        bindings,
    }
};


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

/**
 * @name waltz-survey-template-overview
 *
 * @description
 * This component ...
 */

import {initialiseData, invokeFunction} from "../../common";
import template from './survey-run-overview.html';


const bindings = {
    template: '<',
    run: '<',
    onUpdateDueDate: '<'
};


const initialState = {
    onUpdateDueDate: () => console.log("SRO: No on-update-due-date method provided")
};


function controller() {
    const vm = initialiseData(this, initialState);

    vm.updateDueDate = (data) => {
        invokeFunction(vm.onUpdateDueDate, data.newVal);
    };
}


const component = {
    controller,
    template,
    bindings
};


export default component;
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

import {initialiseData, invokeFunction} from "../../../common";
import template from "./basic-app-selector.html";


const bindings = {
    addLabel: "@?",
    cancelLabel: "@?",
    onCancel: "<",
    onAdd: "<",
    onSelect: "<"
};


const initialState = {
    addLabel: "Add",
    cancelLabel: "Cancel",
    onCancel: () => console.log("No onCancel provided to basic app selector"),
    onAdd: (a) => console.log("No onAdd provided to basic app selector", a),
    onSelect: (a) => console.log("No onSelect provided to basic app selector", a)
};


function controller() {
    const vm = initialiseData(this, initialState);

    vm.add = (app) => {
        if (! app) return ;
        vm.onAdd(app);
    };

    vm.cancel = () => vm.onCancel();

    vm.select = (app) => invokeFunction(vm.onSelect, app);
}


controller.$inject = [];


const component = {
    template,
    bindings,
    controller
};


export default {
    component,
    id: "waltzBasicAppSelector"
};



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

import {initialiseData} from "../../../common/index";
import template from "./enum-value.html";


const bindings = {
    type: "<",
    key: "<",
    showIcon: "<?",
    showPopover: "<?"
};


const initialState = {
    showIcon: true,
    showPopover: true
};


function controller(displayNameService, descriptionService, iconNameService, iconColorService) {
    const vm = initialiseData(this, initialState);

    const refresh = () => {
        vm.name = displayNameService.lookup(vm.type, vm.key) || vm.key;
        vm.description = descriptionService.lookup(vm.type, vm.key);
        vm.icon = iconNameService.lookup(vm.type, vm.key);
        vm.iconColor = iconColorService.lookup(vm.type, vm.key);

        vm.style = {
            "color" : vm.iconColor
        };
    };

    vm.$onInit = () => refresh();
    vm.$onChanges = () => refresh();
}


controller.$inject = [
    "DisplayNameService",
    "DescriptionService",
    "IconNameService",
    "IconColorService"
];


const component = {
    template,
    bindings,
    controller
};


export default {
    component,
    id: "waltzEnumValue"
};

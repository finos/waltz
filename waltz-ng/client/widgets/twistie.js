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

import {initialiseData} from "../common";
import template from './twistie.html';


const bindings = {
    collapsed: '<',
    toggleExpansion: '<'
};


const initialState = {
    collapsed: false,
    toggleExpansion: (collapsed) => console.log("Default handler defined for toggleExpansion - collapsed: ", collapsed)
};


function calcClassNames(collapsed = false) {
    const iconName = collapsed
        ? 'chevron-right'
        : 'chevron-down';

    return [
        'fa',
        'fa-fw',
        `fa-${ iconName }`
    ];
}


function controller() {
    const vm = initialiseData(this, initialState);

    vm.onClick = () => {
        vm.collapsed = ! vm.collapsed;
        if (vm.toggleExpansion) {
            vm.toggleExpansion(vm.collapsed);
        }
    };

    vm.$onChanges = () => {
        vm.clazz = calcClassNames(vm.collapsed);
    };

}


const component = {
    bindings,
    controller,
    template
};


export default component;



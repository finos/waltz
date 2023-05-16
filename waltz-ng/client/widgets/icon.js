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

const bindings = {
    name: "@",
    size: "@",          // lg | 2x | 3x | 4x | 5x
    flip: "@",          // horizontal | vertical
    rotate: "@",        // 90 | 180 | 270
    stack: "@",
    fixedWidth: "@",
    inverse: "@",
    spin: "@"
};


const template = "<span style=\"font-size: smaller;\"><i ng-class=\"$ctrl.classNames\"/></span>";

function controller() {
    const vm = this;
    vm.$onChanges = () => {
        vm.classNames = [
            "fa",
            `fa-${vm.name}`,
            vm.flip ? `fa-flip-${vm.flip}` : "",
            vm.rotate ? `fa-rotate-${vm.rotate}` : "",
            vm.size ? `fa-${vm.size}` : "",
            vm.stack ? `fa-stack-${vm.stack}` : "",
            vm.fixedWidth ? "fa-fw" : "",
            vm.inverse ? "fa-inverse" : "",
            vm.spin ? "fa-spin" : ""
        ];
    };
}


const component = {
    bindings,
    template,
    controller
};


export default component;



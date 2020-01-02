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

import template from "./mini-actions.html";
import {initialiseData} from "../../index";

const bindings = {
    actions: "<",
    ctx: "<"
};


const initialState = {

};


/**
 * Renders a mini action bar, where each action looks like:
 * ```
 * {
 *     type: 'action' | 'link',
 *     name: <string>,
 *     icon: <string>,
 *     predicate:  fn(ctx) => bool,
 *     execute: fn(ctx) => ...        // if type == 'action'
 *     state: <string> => ...         // if type == 'link'
 *     stateParams: <string> => ...   // if type == 'link'
 * }
 * ```
 * @type {{actions: string, ctx: string}}
 */

function controller() {

    const vm = initialiseData(this, initialState);

    function canShow(action) {
        if (action.predicate) {
            return action.predicate(vm.ctx);
        } else {
            return true;
        }
    }

    vm.$onChanges = () => {
        vm.availableActions = _.filter(vm.actions, a => canShow(a));
    };
}


controller.$inject = [];


const component = {
    controller,
    bindings,
    template
};


export default {
    id: "waltzMiniActions",
    component
}
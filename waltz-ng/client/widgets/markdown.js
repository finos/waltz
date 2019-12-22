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

import _ from "lodash";
import {initialiseData} from "../common";


const bindings = {
    text: "<",
    context: "<?"
};


const initialState = {
    variables: {}
};


const template = `
    <span ng-if="$ctrl.text"
          class="waltz-markdown"
          markdown-to-html="$ctrl.markdown">
    </span>`;


function interpolate(templateFn, context = {}, templateStr = "") {
    try {
        return templateFn(context);
    } catch (e) {
        console.log("Failed to interpolate template with context", { context, templateStr, e })
    }
}


function controller() {
    const vm = initialiseData(this, initialState);

    let templateFn = () => "";

    vm.$onChanges = (c) => {
        templateFn = _.template(vm.text, { variable: "ctx"});
        vm.markdown = _.isEmpty(vm.context) ? vm.text : interpolate(templateFn, vm.context, vm.text);
    };
}


/**
 * This component will attempt to render the given `text` binding as markdown.  Using the optional
 * context object allows for simple interpolation of variables.  For example:
 *
 * ```
 *  <waltz-markdown text="'## Hello Simple bindings: ${ ctx.msg } / ${ctx.a.b}'"
 *                  context="{ msg: 'bob!', a: { b: 2 } }">
 *  </waltz-markdown>
 * ```
 *
 * If variables are provided the interpolation is performed by the lodash `_.template` function.
 *
 * @type {{bindings: {text: string, context: string}, controller: controller, template: string}}
 */
const component = {
    bindings,
    controller,
    template
};


export default component;



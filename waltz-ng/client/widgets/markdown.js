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
import Markdown from "../common/svelte/Markdown.svelte";


const bindings = {
    text: "<",
    context: "<?"
};


const initialState = {
    Markdown,
    context: null,
    text: ""
};


const template = `
    <waltz-svelte-component ng-if="$ctrl.text"
                            component="$ctrl.Markdown"
                            context="$ctrl.context"
                            text="$ctrl.text">
    </waltz-svelte-component>`;


function controller() {
    initialiseData(this, initialState);
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
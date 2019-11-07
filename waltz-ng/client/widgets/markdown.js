/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017 Waltz open source project
 * See README.md for more information
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
        console.log("Failed to interpolate template with context", { context, templateStr })
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
 * If variables are provided the interpolation is provided by the lodash `_.template` function.
 *
 * @type {{bindings: {text: string, context: string}, controller: controller, template: string}}
 */
const component = {
    bindings,
    controller,
    template
};


export default component;



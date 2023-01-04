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

import template from "./playpen2.html";
import {select} from "d3-selection";
import {
    symbol, symbolCircle,
    symbols,
    symbolWye,
} from "d3-shape";
import _ from "lodash";
import {getSymbol} from "../../common/svg-icon";

const initialState = {}

function controller(serviceBroker, $element) {


    const svgElem = select($element.find("svg")[0]);


    const xs = [66, 66]; //, 7, 32];

    const selection = svgElem
        .selectAll("path")
        .data(xs);

    selection
        .enter()
        .append("path")
        .attr("d", (d, i) => i % 2 === 0 ? getSymbol("hourglass", d) : symbol().type(symbolCircle).size(d)())
        .attr("transform", (d, i) => `translate(${i * 50 + 10}, 10)`)
        .attr("stroke", "red")
        .attr("stroke-width", 0.1)
        .attr("fill", "yellow")
        .attr("stroke-linecap", "round");

}


controller.$inject = ["ServiceBroker", "$element"];


const view = {
    template,
    controller,
    controllerAs: "$ctrl",
    bindToController: true,
    scope: {}
};


export default view;

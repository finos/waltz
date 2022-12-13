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
    symbol,
    symbols,
    symbolSquare,
    symbolWye,
} from "d3-shape";
import _ from "lodash";


const initialState = {}

function controller(serviceBroker, $element) {

    function xPath(size) {
        size = Math.sqrt(size);
        return "M" + (-size / 2) + "," + (-size / 2) +
            "l" + size + "," + size +
            "m0," + -(size) +
            "l" + (-size) + "," + size;
    }

    function desktop(size) {
        size = Math.sqrt(size);
        return `M${size * -1 / 8} ${size * 1 / 2} L${size * -3 / 8} ${size * 5 / 8} L${size * 3 / 8} ${size * 5 / 8} L${size * 1 / 8} ${size * 1 / 2} L${size * -1 / 8} ${size * 1 / 2}
        M${size * -1 / 2} ${size * -1 / 2}
         C ${size * -3 / 4} ${size * -1 / 2} ${size * -3 / 4} ${size * -1 / 2} ${size * -3 / 4} ${size * -1 / 4}
         L${size * -3 / 4} ${size * 1 / 4}
         C${size * -3 / 4} ${size * 1 / 2} ${size * -3 / 4} ${size * 1 / 2} ${size * -1 / 2} ${size * 1 / 2}
         L${size * 3 / 4} ${size * 1 / 2}
         L${size * 3 / 4} ${size * -1 / 2}
         L${size * -1 / 2} ${size * -1 / 2}`
        // return `M${size * -1/8} ${size * 1/2} L${size * -3/8} ${size * 5/8} L${size * 3/8} ${size * 5/8} L${size * 1/8} ${size * 1/2} L${size * -1/8} ${size * 1/2}
        // M${size * -3/4} ${size * -1/2}  L${size * -3/4} ${size * 1/2} L${size * 3/4} ${size * 1/2} L${size * 3/4} ${size * -1/2} L${size * -3/4} ${size * -1/2}`
    }

    const customSymbolTypes = {
        "cross": xPath,
        "desktop": desktop
    };

    const customSymbol = function () {

        let type, size = 64;

        function symbol(d, i) {
            const customSymbol = _.get(customSymbolTypes, [type.call(this, d, i)]);
            return customSymbol(size.call(this, d, i));
        }

        symbol.type = function (tp) {
            if (!arguments.length) return type;
            type = typeof tp === "function" ? tp : constant(tp);
            return symbol;
        };

        symbol.size = function (sz) {
            if (!arguments.length) return size;
            size = typeof sz === "function" ? sz : constant(sz);
            return symbol;
        };

        return symbol;
    };

    const svgElem = select($element.find("svg")[0]);


    function constant(x) {
        return function () {
            return x;
        };
    }

    function getSymbol(type, size) {
        size = size || 64;
        if (symbols.indexOf(type) !== -1) {
            return symbol().type(type).size(size)();
        } else {
            return customSymbol().type(type).size(size)();
        }
    }

    const xs = [66, 66]; //, 7, 32];

    const selection = svgElem
        .selectAll("path")
        .data(xs);

    selection
        .enter()
        .append("path")
        .attr("d", (d, i) => i % 2 === 0 ? getSymbol("desktop", d) : symbol().type(symbolWye).size(d)())
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

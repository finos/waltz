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
import {select} from "d3-selection";
import {scaleLinear} from "d3-scale";
import "d3-selection-multi";
import { initialiseData } from "../../../common";

const bindings = {
    scores: "<",
    range: "<",
    ratingSchemeItems: "<"
};

const emptyScores = {R: 0, A: 0, G: 0, total: 0};


const initialState = {
    scores: Object.assign({}, emptyScores)
};


const template = "<svg class=\"rag-line\"></svg>";


function controller($element) {

    const width = 250;
    const height = 3;

    const vm = initialiseData(this, initialState);

    const svg = select($element[0])
        .select("svg")
        .attrs({ width: `${width}px`, height: `${height}px` });

    const update = (scores, range = [0, 0], ratingSchemeItems) => {
        const xScale = scaleLinear()
            .domain([0, range[1]])
            .range([0, width]);

        let ptr = 0;
        const data = _
            .chain(ratingSchemeItems)
            .filter(r => scores[r.rating] > 0)
            .map(r => {
                const width = scores[r.rating];
                const result = Object.assign({}, r, { start: ptr, width });
                ptr = ptr + width;
                return result;
            })
            .value();

        const rects = svg
            .selectAll("rect")
            .data(data, d => d.rating);

        const newRects = rects
            .enter()
            .append("rect")
            .attr("fill", d => d.color)
            .attr("height", height)
            .attr("y", 0);

        newRects.merge(rects)
            .attr("x", d => xScale(d.start))
            .attr("width", d => xScale(d.width));

        rects.exit()
            .remove();
    };


    vm.$onChanges = () => {
        if (!vm.scores) {
            vm.scores = Object.assign({}, emptyScores);
        }

        if (vm.range && vm.ratingSchemeItems) {
            update(vm.scores, vm.range, vm.ratingSchemeItems);
        }
    };

}


controller.$inject = [
    "$element"
];

const component = {
    template,
    bindings,
    controller
};


export default {
    id: "waltzRagLine",
    component
};

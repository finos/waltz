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

import {initialiseData} from "../../common";
import {select} from "d3-selection";
import {scaleLinear} from "d3-scale";

const template = "";


const bindings = {
    config: "<"
};

const NUM_WEEKS = 53;

const DIMENSIONS = {
    margins: {
        left: 50,
        right: 10,
        top: 50,
        bottom: 10
    },
    cellSize: 12
}

const initData = {

};

function mkData() {
    return _
        .chain(_.range(NUM_WEEKS))
        .map(d => [
            Math.random() * 10,
            Math.random() * 80,
            Math.random() * 70,
            Math.random() * 80,
            Math.random() * 90,
            Math.random() * 60,
            Math.random() * 10
        ])
        .value();
}



function controller($element) {
    const vm = initialiseData(this, initData);

    vm.$onInit = () => {
        console.log("on init");
        const d = mkData();

        const svg = select($element[0])
            .append("svg")
            .attr("width", DIMENSIONS.margins.left + DIMENSIONS.margins.right + (NUM_WEEKS * (DIMENSIONS.cellSize + 2)))

        const gWeeks = svg
            .append("g")
            .classed("wch-weeks", true)
            .attr("transform", `translate(${DIMENSIONS.margins.left}, ${DIMENSIONS.margins.top})`)

        const gWeek = gWeeks
            .selectAll("g.wch-week")
            .data(d)
            .enter()
            .append("g")
            .classed(".wch-week", true)
            .attr("transform", (d, idx) => `translate(${idx * (DIMENSIONS.cellSize + 2) }, 0)`);

        const scale = scaleLinear()
            .domain([0, 100])
            .range(["#ffffff", "#a5f196"]);

        gWeek
            .selectAll("rect.wch-day")
            .data(d => d)
            .enter()
            .append("rect")
            .classed("wch-day", true)
            .classed("wch-day2", (d) => console.log(d, scale(d)))
            .attr("width", DIMENSIONS.cellSize)
            .attr("height", DIMENSIONS.cellSize)
            .attr("rx", 2)
            .attr("ry", 2)
            .attr("y", (d, i) => i * DIMENSIONS.cellSize)
            .attr("fill", (d) => scale(d))
            .attr("stroke", "#9cd48e");
    };

    vm.$onChanges = () => {
        console.log("On change")
    }

}


controller.$inject = ["$element"];

export default {
    id: "waltzCalendarHeatmap",
    component: {
        template,
        bindings,
        controller
    }
};
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
import {nest} from "d3-collection";
import {scaleLinear} from "d3-scale";
import {select} from "d3-selection";
import moment from "moment";

const bindings = {
    config: "<"
};


function getDates(start, stop) {
    const dateArray = [];
    let currentDate = moment(start);
    const stopDate = moment(stop);
    while (currentDate <= stopDate) {
        dateArray.push( moment(currentDate).format("YYYY-MM-DD") )
        currentDate = moment(currentDate).add(1, "days");
    }
    return dateArray;
}

const NUM_WEEKS = 53;


const DIMENSIONS = {
    margins: {
        left: 50,
        right: 10,
        top: 50,
        bottom: 10
    },
    cellSize: 14
}


const initData = {

};

const dates = getDates(
    moment().subtract(   1, "years"),
    moment());

global.moment = moment;
global.dates = dates;

function mkRawData() {

    const dayDist = {
        0: 20,
        1: 70,
        2: 80,
        3: 90,
        4: 100,
        5: 70,
        6: 10
    };

    return _
        .chain(dates)
        .map(d => {
            const dt = moment(d);
            const day = dt.day();
            const val = Math.random() * dayDist[day];
            return {
                date: moment(d),
                day,
                val
            };
        })
        .value();
}


function prepareData(data) {
    const byYearWeek = nest()
        .key(d => d.date.year())
        .key(d => d.date.week())
        .entries(data);

    let acc = 0;
    _.forEach(byYearWeek, d => {
        d.offset = acc;
        acc += d.values.length ;
        d.endOffset = acc;
    });

    return byYearWeek;
}

function draw(data, holder) {
    const maxOffset = _.max(_.map(data, "endOffset"));

    const w = DIMENSIONS.margins.left + DIMENSIONS.margins.right + (maxOffset * DIMENSIONS.cellSize);
    const h = DIMENSIONS.margins.top + DIMENSIONS.margins.bottom + (7 * DIMENSIONS.cellSize)

    const svg = select(holder)
        .append("svg")
        .style("border", "1px dashed red")
        .attr("viewBox", `0 0 ${w} ${h}`)
        .attr("width", 900)

    const gYears = svg
        .append("g")
        .classed("wch-years", true)
        .attr("transform", `translate(${DIMENSIONS.margins.left}, ${DIMENSIONS.margins.top})`)

    const gWeeks = gYears
        .selectAll("g.wch-year")
        .data(data)
        .enter()
        .append("g")
        .classed("wch-year", true)
        .attr("transform", d => `translate(${DIMENSIONS.cellSize * d.offset}, 0)`)

    const gWeek = gWeeks
        .selectAll("g.wch-week")
        .data(d => d.values)
        .enter()
        .append("g")
        .classed(".wch-week", true)
        .attr("transform", (d, idx) => `translate(${idx * (DIMENSIONS.cellSize)}, 0)`);

    const colorScale = scaleLinear()
        .domain([20, 100])
        .range(["#e7fae2", "#7df563"]);

    gWeek
        .selectAll("rect.wch-day")
        .data(d => d.values)
        .enter()
        .append("rect")
        .classed("wch-day", true)
        .attr("width", DIMENSIONS.cellSize - 4)
        .attr("height", DIMENSIONS.cellSize - 4)
        .attr("rx", 2)
        .attr("ry", 2)
        .attr("y", (d, i) => d.date.day() * DIMENSIONS.cellSize)
        .attr("fill", (d) => d.val > 20
                ? colorScale(d.val)
                : "#fafafa")
        .attr("stroke", d => d.val > 20
            ? "#93d489"
            : "#ddd")
        .on("mouseover", d => console.log(d.date.day(), d.val))
}

function controller($element) {
    const vm = initialiseData(this, initData);

    vm.$onInit = () => {
        draw(prepareData(mkRawData()), $element[0]);
    };

    vm.$onChanges = () => {
        console.log("On change")
    };
}


controller.$inject = ["$element"];

export default {
    id: "waltzCalendarHeatmap",
    component: {
        template: "",
        bindings,
        controller
    }
};
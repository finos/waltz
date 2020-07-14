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

import {initialiseData} from "../..";
import {nest} from "d3-collection";
import {scaleLinear, scaleSqrt} from "d3-scale";
import {select} from "d3-selection";
import moment from "moment";

const bindings = {
    data: "<",
    onSelectDate: "<"
};

const COLORS = {
    cellBorder: "#93d489",
    filledCellRange: ["#e7fae2", "#07ed4a"],
    emptyCellFill: "#fafafa",
    emptyCellBorder: "#ddd",
    label: "#8b8888"
};

const DIMENSIONS = {
    margins: {
        left: 50,
        right: 10,
        top: 32,
        bottom: 10
    },
    cellSize: 14,
    fontSize: 12
};

const initData = {

};


function getDates(start, stop) {
    const dateArray = [];
    let currentDate = moment(start);
    const stopDate = moment(stop);
    while (currentDate <= stopDate) {
        dateArray.push( moment(currentDate).format("YYYY-MM-DD"));
        currentDate = moment(currentDate).add(1, "days");
    }
    return dateArray;
}


function prepareData(data = []) {

    const rawDates = getDates(
        moment().subtract(   12, "months"),
        moment());

    const dataByDate = _.keyBy(data, "date");

    return _
        .chain(rawDates)
        .map(d => {
            const dt = moment(d);
            const day = dt.day();
            return {
                dateStr: d,
                date: moment(d),
                day,
                count:  _.get(dataByDate, [d, "count"], 0)
            };
        })
        .value();
}


function nestData(preparedData = []) {
    const byYearWeek = nest()
        .key(d => d.date.year())
        .key(d => d.date.week())
        .entries(preparedData);

    let acc = 0;
    _.forEach(byYearWeek, d => {
        d.offset = acc;
        acc += d.values.length ;
        _.forEach(d.values, (v,idx) => v.offset = d.offset + idx);
        d.endOffset = acc;
    });

    return byYearWeek;
}


function drawDayLabels(svg) {
    svg.append("g")
        .classed("wch-day-labels", true)
        .attr("transform", `translate(${DIMENSIONS.margins.left - 5}, ${DIMENSIONS.margins.top})`)
        .selectAll("text.wch-day-label")
        .data([{label: "Mon", day: 1}, {label: "Wed", day: 3}, {label: "Fri", day: 5}])
        .enter()
        .append("text")
        .classed("wch-day-label", true)
        .attr("text-anchor", "end")
        .attr("fill", COLORS.label)
        .attr("font-size", DIMENSIONS.fontSize)
        .attr("dy", d => DIMENSIONS.cellSize * d.day + DIMENSIONS.fontSize - 2)
        .text(d => d.label);
}

/**
 * Drawing the labels for the months is tricky.
 * First we construct a map of the first occurences of
 * year/month combinations (the key of the map)
 * which has a reference to the offset it first
 * occurred at.  We can then take the values of
 * that map to creat the labels.
 *
 * @param svg
 * @param rawData
 * @param nestedData
 */
function drawMonthLabels(svg, rawData, nestedData) {
    const curr = moment();
    const monthOffsets = {};
    _.forEach(nestedData, y => {
        curr.year(y.key);
        _.forEach(y.values, w => {
            curr.isoWeek(w.key);
            const m = curr.month();
            const accKey = `${y.key}-${m}`;
            if (! monthOffsets[accKey]) {
                monthOffsets[accKey] = {
                    year: y.key,
                    month: m,
                    offset: w.offset
                };
            }
        })
    });

    const months = ["Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"];

    svg.append("g")
        .attr("transform", `translate(${DIMENSIONS.margins.left}, ${DIMENSIONS.margins.top - (DIMENSIONS.fontSize + 2)})`)
        .selectAll("text.wch-month-label")
        .data(_.values(monthOffsets))
        .enter()
        .append("text")
        .attr("fill", COLORS.label)
        .attr("font-size", DIMENSIONS.fontSize)
        .attr("transform", d => `translate(${d.offset * DIMENSIONS.cellSize} 0)`)
        .text(d => months[d.month]);
}


function draw(rawData, holder, onSelect) {
    const nestedData = nestData(rawData);
    const maxOffset = _.max(_.map(nestedData, "endOffset"));

    const w = DIMENSIONS.margins.left + DIMENSIONS.margins.right + (maxOffset * DIMENSIONS.cellSize);
    const h = DIMENSIONS.margins.top + DIMENSIONS.margins.bottom + (7 * DIMENSIONS.cellSize);

    const actualMax = _.get(_.maxBy(rawData, 'count'), ['count'], 0);

    const colorScale = (actualMax > 100)
        ? scaleSqrt()
            .domain([0, _.min([1000, actualMax])])
            .range(COLORS.filledCellRange)
            .clamp(true)
        : scaleLinear()
            .domain([0, _.min([1000, actualMax])])
            .range(COLORS.filledCellRange)
            .clamp(true);

    const svg = select(holder)
        .append("svg")
        .style("max-width", "1200px")
        .attr("viewBox", `0 0 ${w} ${h}`)
        .attr("preserveAspectRatio", "xMinYMin meet");

    drawDayLabels(svg);

    const gYears = svg
        .append("g")
        .classed("wch-years", true)
        .attr("transform", `translate(${DIMENSIONS.margins.left}, ${DIMENSIONS.margins.top})`)

    const gWeeks = gYears
        .selectAll("g.wch-year")
        .data(nestedData)
        .enter()
        .append("g")
        .classed("wch-year", true)
        .attr("transform", d => `translate(${DIMENSIONS.cellSize * d.offset }, 0)`)

    const gWeek = gWeeks
        .selectAll("g.wch-week")
        .data(d => d.values)
        .enter()
        .append("g")
        .classed(".wch-week", true)
        .attr("transform", (d, idx) => `translate(${idx * (DIMENSIONS.cellSize)}, 0)`);

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
        .attr("y", (d) => d.date.day() * DIMENSIONS.cellSize)
        .attr("fill", (d) => d.count > 0
            ? colorScale(d.count)
            : COLORS.emptyCellFill)
        .attr("stroke", d => d.count > 0
            ? COLORS.cellBorder
            : COLORS.emptyCellBorder)
        .on("click", d => onSelect(d.date.format('YYYY-MM-DD')));

    drawMonthLabels(svg, rawData, nestedData)
}


function controller(serviceBroker, $element, $timeout) {
    const vm = initialiseData(this, initData);

    vm.$onInit = () => {
    };

    vm.$onChanges = (c) => {
        if(c.data && vm.data != null){
            draw(prepareData(vm.data), $element[0], d => $timeout(() => vm.onSelectDate(d)));
        }
    };
}


controller.$inject = [
    "ServiceBroker",
    "$element",
    "$timeout"
];


export default {
    id: "waltzCalendarHeatmap",
    component: {
        template: "",
        bindings,
        controller
    }
};
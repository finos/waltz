import template from "./milestone-chart.html";
import {initialiseData} from "../../../common";
import {toStackData} from "../milestone-utils";
import {select} from "d3-selection";
import {scaleBand, scaleLinear, scaleOrdinal, scaleUtc} from "d3-scale";
import {area, stack} from "d3-shape";
import {extent, max} from "d3-array";
import {axisBottom, axisLeft} from "d3-axis";


const bindings = { rawData: "<"};



const keyNames = {
    r: "Sell",
    a: "Hold",
    g: "Buy"
};

const initialState = {
};

const margin = {
    left: 20,
    right: 20,
    top: 10,
    bottom: 10
};

const subChartTotalHeight = 250; // includes margins which are added by the scaleBand
const chartWidth = 800;
const subChartMargin = 10;
const subChartWidth = chartWidth - (2 * subChartMargin);


function setup(root, dimensions) {
    return root
        .select(".milestone-chart")
        .attr("viewBox", `0 0 ${dimensions.width + margin.left + margin.right} ${dimensions.height + margin.top + margin.bottom}`)
        .append("g")
        .classed("milestone-chart-body", true)
        .attr("transform", `translate(${margin.left} ${margin.top})`);
}


const color = scaleOrdinal()
    .domain(['r', 'a', 'g'])
    .range(["#fd4d4d", "#eeb65f", "#a8e761"]);


const stacker = stack()
    .keys(['r', 'a', 'g'])
    .value((d, k) => d.values[k].length)


function setupSubCharts(root, height) {
    const subChart = root
        .append("g")
        .classed("sub-chart-body", true)
        .attr("transform", `translate(${margin.left} ${margin.top})`);

    subChart
        .append("g")
        .classed("chart", true);
    subChart
        .append("g")
        .classed("x-axis", true);
    subChart
        .append("g")
        .classed("y-axis", true);

    subChart.each(function(d, i) {
        console.log("each", {d, i})
        const series = stacker(d.stackData);
        console.log({series})

        const y = scaleLinear()
            .domain([0, max(series, d => max(d, d => d[1]))]).nice()
            .range([height - margin.bottom, margin.top]);

        const x = scaleUtc()
            .domain(extent(d.stackData, d => d.k))
            .range([margin.left, subChartWidth - margin.right])

        const areaFn = area()
            .x(d => x(d.data.k))
            .y0(d => y(d[0]))
            .y1(d => y(d[1]));

        const xAxis = g => g
            .attr("transform", `translate(0,${height - margin.bottom})`)
            .call(axisBottom(x)
                .ticks(subChartWidth / 80)
                .tickSizeOuter(0));

        const yAxis = g => g
            .attr("transform", `translate(${margin.left},0)`)
            .call(axisLeft(y))
            .call(g => g.select(".domain").remove());

        const elem = select(this);
        elem.select("g.chart")
            .selectAll("path")
            .data(series)
            .enter()
            .append("path")
            .attr("fill", (d) => color(d.key))
            .attr("stroke", "#555")
            .attr("stroke-width", 0.5)
            .attr("d", areaFn)
        // .append("title")
        // .text(({key}) => keyNames[key]);

        elem.select("g.x-axis")
            .call(xAxis);

        elem.select("g.y-axis")
            .call(yAxis);

    });


    return subChart;
}


function drawSubCharts(selection, height) {

    const subCharts = selection
        .call(setupSubCharts, height);

    // subCharts
    //     .append("rect")
    //     .attr("width", subChartWidth)
    //     .attr("height", height)
    //     .style("stroke", d => console.log({d}) || "blue")
    //     .style("stroke-width", 3)
    //     .style("fill", "#ccc");
}




function controller($element) {
    const vm = initialiseData(this, initialState);

    vm.$onChanges = () => {
        const groupedByVenue = _.groupBy(
            vm.rawData,
            d => d.id_b);

        const stacks = _.map(
            groupedByVenue,
            (v, k) => ({k, stackData: toStackData(v)}));

        const dimensions = {width: chartWidth, height: subChartTotalHeight * stacks.length}

        const root = select($element[0]);
        const svg = setup(root, dimensions);

        const subChartVerticalScale = scaleBand()
            .range([0, dimensions.height])
            .domain(_.map(stacks, s => s.k))
            .paddingOuter(0.1)
            .paddingInner(0.2);

        const subCharts = svg
            .selectAll(".sub-chart")
            .data(stacks)
            .enter()
            .append("g")
            .classed("sub-chart", true)
            .attr("transform", d => `translate(${subChartMargin} ${subChartVerticalScale(d.k)})`);

        subCharts.call(drawSubCharts, subChartVerticalScale.bandwidth());


    }
}

controller.$inject = ["$element"];

const component = {
    bindings,
    controller,
    template
};


export default {
    id: "waltzMilestoneChart",
    component
};
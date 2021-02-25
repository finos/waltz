import template from "./milestone-chart.html";
import {initialiseData} from "../../../common";
import {calcDateExtent, toStackData} from "../milestone-utils";
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

const color = scaleOrdinal()
    .domain(['r', 'a', 'g'])
    .range(["#fd4d4d", "#eeb65f", "#a8e761"]);

function setup(root, dimensions) {
    const grads = root
        .select("defs")
        .selectAll("linearGradient")
        .data(color.domain())
        .enter()
        .append("linearGradient")
        .attr("id", d => `grad_${d}`)
        .attr("x1", "0%")
        .attr("y1", "0%")
        .attr("x2", "100%")
        .attr("y2", "0%");

    grads.append("stop")
        .attr("offset", "0%")
        .style("stop-color", d => color(d))
        .style("stop-opacity", "1");

    grads.append("stop")
        .attr("offset", "50%")
        .style("stop-color", d => color(d))
        .style("stop-opacity", "1");

    grads.append("stop")
        .attr("offset", "100%")
        .style("stop-color", "#fff")
        .style("stop-opacity", "1");

    return root
        .select(".milestone-chart")
        .attr("viewBox", `0 0 ${dimensions.width + margin.left + margin.right} ${dimensions.height + margin.top + margin.bottom}`)
        .append("g")
        .classed("milestone-chart-body", true)
        .attr("transform", `translate(${margin.left} ${margin.top})`);
}




const stacker = stack()
    .keys(['r', 'a', 'g'])
    .value((d, k) => d.values[k].length)


function setupSubCharts(root, height, dateScale) {
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


        const xAxis = g => g
            .attr("transform", `translate(0,${height - margin.bottom})`)
            .call(axisBottom(dateScale)
                .ticks(subChartWidth / 80)
                .tickSizeOuter(0));

        const yAxis = g => g
            .attr("transform", `translate(${margin.left},0)`)
            .call(axisLeft(y))
            .call(g => g.select(".domain").remove());

        const elem = select(this);

        elem.select("g.chart")
            .selectAll("g")
            .data(series)
            .enter()
            .append("g")
            .selectAll("rect")
            .data(d => d)
            .enter()
            .append("rect")
            .attr("stroke", "none")
            .attr("stroke-width", 0.5)
            .attr("x", d => dateScale(d.data.s))
            .attr("y", d => y(d[1]))
            .attr("height", d => y(d[0]) - y(d[1]))
            .attr("width", d => dateScale(d.data.e || dateScale.domain()[1]) - dateScale(d.data.s))
            .classed("fade-out", d => _.isUndefined(d.data.e))
            .attr("fill", function (d)  {
                const p = select(this.parentNode).datum();
                return _.isUndefined(d.data.e)
                    ? `url(#grad_${p.key})`
                    : color(p.key);
            })
        // .append("title")
        // .text(({key}) => keyNames[key]);

        elem.select("g.x-axis")
            .call(xAxis);

        elem.select("g.y-axis")
            .call(yAxis);

    });


    return subChart;
}


function drawSubCharts(selection, height, dateScale) {

    const subCharts = selection
        .call(setupSubCharts, height, dateScale);

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

        const dateScale = scaleUtc()
            .domain(calcDateExtent(vm.rawData, 30 * 12))
            .range([margin.left, subChartWidth - margin.right])

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

        subCharts.call(drawSubCharts, subChartVerticalScale.bandwidth(), dateScale);


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
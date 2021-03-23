import {mouse, select} from "d3-selection";
import template from "./stacked-bar-chart.html";
import {scaleLinear, scaleOrdinal, scaleUtc} from "d3-scale";
import {axisBottom, axisLeft} from "d3-axis";
import {max} from "d3-array";
import {stack} from "d3-shape";
import {initialiseData} from "../../../common";
import _ from "lodash";

const bindings = {
    rawData: "<",
    dateExtent: "<",
    onSelect:"<?"
};

const initialState = {
    rawData: [],
    onSelect: (d, t) => console.log("Default stacked-bar-chart on-select handler", d, t)
};

const width = 400, height = 100;
const margin = {
    left: 20,
    right: 20,
    top: 10,
    bottom: 10
};

const keyNames = {
    r: "Sell",
    a: "Hold",
    g: "Buy"
};


const color = scaleOrdinal()
    .domain(['r', 'a', 'g'])
    .range(["#fd4d4d", "#eeb65f", "#a8e761"]);


const stacker = stack()
    .keys(['r', 'a', 'g'])
    .value((d, k) => d.values[k].length);


function setup(root) {
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

    const svg = root
        .select(".stacker")
        .attr("viewBox", `0 0 ${width + margin.left + margin.right} ${height + margin.top + margin.bottom}`)
        .append("g")
        .attr("transform", `translate(${margin.left} ${margin.top})`);

    svg.append("g")
        .classed("chart", true);
    svg.append("g")
        .classed("x-axis", true);
    svg.append("g")
        .classed("y-axis", true);
    return svg;
}


function controller($element, $scope) {
    const vm = initialiseData(this, initialState);

    const root = select($element[0]);

    const svg = setup(root);

    vm.$onChanges = () => {

        const series = stacker(vm.rawData);

        const y  = scaleLinear()
            .domain([0, max(series, d => max(d, d => d[1]))]).nice()
            .range([height - margin.bottom, margin.top]);

        const x = scaleUtc()
            .domain(vm.dateExtent)
            .range([margin.left, width - margin.right]);

        const xAxis = g => g
            .attr("transform", `translate(0,${height - margin.bottom})`)
            .call(axisBottom(x)
                .ticks(width / 80)
                .tickSizeOuter(0));

        const yAxis = g => g
            .attr("transform", `translate(${margin.left},0)`)
            .call(axisLeft(y)
                .ticks(height / 20))
            .call(g => g.select(".domain").remove());

        svg
            .select("g.chart")
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
            .attr("x", d => x(d.data.s))
            .attr("y", d => y(d[1]))
            .attr("height", d => y(d[0]) - y(d[1]))
            .attr("width", d => x(d.data.e || x.domain()[1]) - x(d.data.s))
            .classed("fade-out", d => _.isUndefined(d.data.e))
            .attr("fill", function (d)  {
                const p = select(this.parentNode).datum();
                return _.isUndefined(d.data.e)
                    ? `url(#grad_${p.key})`
                    : color(p.key);
            })
            .on("click.select", d => {
                const mousePosition = mouse(svg.node())[0];
                const selectedDate = x.invert(mousePosition);
                $scope.$applyAsync(() => vm.onSelect(d, selectedDate));
                // vm.onSelect(d, selectedDate);
            });

        svg.select("g.x-axis")
            .call(xAxis);

        svg.select("g.y-axis")
            .call(yAxis);

    };
}

controller.$inject = ["$element", "$scope"];

const component = {
    bindings,
    controller,
    template
};

export default {
    id: "waltzStackedBarChart",
    component
}
import {select} from "d3-selection";
import template from "./stacker.html";
import {scaleLinear, scaleOrdinal, scaleUtc} from "d3-scale";
import {axisBottom, axisLeft} from "d3-axis";
import {max, extent} from "d3-array";
import {area, stack} from "d3-shape";
import {initialiseData} from "../../../common";

const bindings = {
    rawData: "<"
};

const initialState = {
    rawData: []
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
    .value((d, k) => d.values[k].length)


function setup(root) {
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


function controller($element) {
    const vm = initialiseData(this, initialState);

    const root = select($element[0]);
    const svg = setup(root);

    vm.$onChanges = () => {
        console.log({raw: vm.rawData});
        const series = stacker(vm.rawData);
        console.log({series});

        const y  = scaleLinear()
            .domain([0, max(series, d => max(d, d => d[1]))]).nice()
            .range([height - margin.bottom, margin.top]);

        const xOld = scaleLinear()
            .domain(extent(vm.rawData, d => d.k))
            .range([margin.left, width - margin.right]);

        const x = scaleUtc()
            .domain(extent(vm.rawData, d => d.k))
            .range([margin.left, width - margin.right])

        console.log({x, y});
        global.scales = {x,y};
        global.rawData = vm.rawData;
        global.series = series;

        const areaFn = area()
            .x(d => x(d.data.k))
            .y0(d => y(d[0]))
            .y1(d => y(d[1]));

        const xAxis = g => g
            .attr("transform", `translate(0,${height - margin.bottom})`)
            .call(axisBottom(x)
                .ticks(width / 80)
                .tickSizeOuter(0));

        const yAxis = g => g
            .attr("transform", `translate(${margin.left},0)`)
            .call(axisLeft(y))
            .call(g => g.select(".domain").remove());

        svg.select("g.chart")
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

        svg.select("g.x-axis")
            .call(xAxis);

        svg.select("g.y-axis")
            .call(yAxis);

    };
}

controller.$inject = ["$element"];

const component = {
    bindings,
    controller,
    template
}

export default {
    id: "waltzStacker",
    component
}
import _ from "lodash";
import {scaleLinear} from 'd3-scale';
import {select} from 'd3-selection';
import "d3-selection-multi";

const bindings = {
    values: '=',
    max: '='
};


function calculateLayoutData(values = [], xScale) {
    let last = xScale(0);
    return _.map(values, v => {
        const d = {
            x: last,
            width: v ? xScale(v) : 0
        };
        last += d.width;
        return d;
    });
}


function controller($element) {

    const holder = select($element[0])
        .select('.waltz-simple-stack-chart')

    const svg = holder
        .append('svg')
        .attr('width', 0);

    const ambient = svg
        .append('rect')
        .classed('wssc-ambient', true);


    const update = (values = [], max) => {

        const height = 24;
        // TODO: 1026: d3v4 - can't work out client width...
        const width = $element[0].clientWidth || 150;

        svg
            .attrs({ width: `${width}`, height: `${height}` });

        const xScale = scaleLinear()
            .domain([0, max])
            .range([
                10,
                width - 30
            ]);

        const coords = calculateLayoutData(values, xScale);

        svg.selectAll('.wssc-stack')
            .data(values)
            .enter()
            .append('rect')
            .classed('wssc-stack', true)
            .attr("class", (d, idx) => `wssc-stack-${idx}`)
            .attrs({
                y: 3,
                height: height - 6
            })
            .attr("x", (d, idx) => coords[idx].x)
            .attr("width", (d, idx) => coords[idx].width);

        ambient
            .attrs({
                width,
                height,
                x: 0,
                y: 0
            });
    };


    const vm = this;

    vm.$onChanges = changes => {
        if (vm.values) {
            update(vm.values, vm.max);
        }
    };
}


controller.$inject = [
    '$element',
    '$scope'
];


const component = {
    bindings,
    controller,
    template: '<div class="waltz-simple-stack-chart"></div>'
};


export default component;
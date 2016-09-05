import _ from "lodash";
import d3 from "d3";


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

    const holder = d3
        .select($element[0])
        .select('.waltz-simple-stack-chart')

    const svg = holder
        .append('svg')
        .attr('width', 0);

    const ambient = svg
        .append('rect')
        .classed('wssc-ambient', true);


    const update = (values = [], max) => {

        const height = 24;
        const width = holder[0][0].clientWidth;

        svg
            .attr({ width: `${width}`, height: `${height}` });

        const xScale = d3
            .scale
            .linear()
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
            .attr({
                y: 3,
                height: height - 6
            })
            .attr("x", (d, idx) => coords[idx].x)
            .attr("width", (d, idx) => coords[idx].width);

        ambient
            .attr({
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
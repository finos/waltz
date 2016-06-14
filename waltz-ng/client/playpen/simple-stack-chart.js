import _ from "lodash";
import d3 from "d3";


const BINDINGS = {
    values: '=',
    max: '='
};


function calculateLayoutData(values = [], xScale) {
    let last = 0;
    return _.map(values, v => {
        const d = {
            x: last,
            width: xScale(v)
        };
        last += d.width;
        return d;
    });
}


function controller($element, $scope) {

    const width = 40;
    const height = 16;

    const svg = d3.select($element[0])
        // .append('svg')
        .attr({ width: `${width}px`, height: `${height}px` });

    const update = (values=[], max) => {
        const xScale = d3.scale.linear().domain([0, max]).range([0, width]);

        const coords = calculateLayoutData(values, xScale);
        svg.selectAll('rect')
            .data(values)
            .enter()
            .append('rect')
            .attr("class", (d, idx) => `stack-${idx}`)
            .attr({ y: 0, height })
            .attr("x", (d, idx) => coords[idx].x)
            .attr("width", (d, idx) => coords[idx].width);

    };


    $scope.$watchGroup(
        ['ctrl.values', 'ctrl.max'],
        ([values, max]) => {
            if (values && max) {
                update(values, max);
            }
        });

}


controller.$inject = [
    '$element',
    '$scope'
];


const directive = {
    restrict: 'E',
    replace: true,
    scope: {},
    bindToController: BINDINGS,
    controller,
    controllerAs: 'ctrl',
    template: '<svg class="rag-line"></svg>'
};


export default () => directive;
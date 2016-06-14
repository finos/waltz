import d3 from "d3";
import {red, amber, green, grey} from "../common/colors";


const BINDINGS = {
    scores: '=',
    range: '='
};



function controller($element, $scope) {

    const width = 250;
    const height = 3;

    const svg = d3.select($element[0])
        .attr({ width: `${width}px`, height: `${height}px` });

    const rRect = svg.append('rect')
        .attr({ fill: red, height, y: 0 });

    const aRect = svg.append('rect')
        .attr({ fill: amber, height, y: 0 });

    const gRect = svg.append('rect')
        .attr({ fill: green, height, y: 0 });

    const zRect = svg.append('rect')
        .attr({ fill: grey, height, y: 0 });


    const update = (scores, range = [0, 0]) => {
        const xScale = d3.scale.linear().domain([0, range[1]]).range([0, width]);

        const r = { x: 0, width : xScale(scores.R || 0) };
        const a = { x: 0 + r.width, width : xScale(scores.A || 0) };
        const g = { x: a.x + a.width, width: xScale(scores.G || 0) };
        const z = { x: g.x + g.width, width : xScale(scores.Z || 0) };

        rRect.attr(r);
        aRect.attr(a);
        gRect.attr(g);
        zRect.attr(z);
    };


    $scope.$watchGroup(
        ['ctrl.scores', 'ctrl.range'],
        ([scores, range]) => {
            if (scores && range) {
                update(scores, range);
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
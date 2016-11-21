import {red, amber, green, grey} from "../../../common/colors";
import {select} from 'd3-selection';
import {scaleLinear} from 'd3-scale';
import 'd3-selection-multi';

const BINDINGS = {
    scores: '=',
    range: '='
};



function controller($element, $scope) {

    const width = 250;
    const height = 3;

    const svg = select($element[0])
        .attrs({ width: `${width}px`, height: `${height}px` });

    const rRect = svg.append('rect')
        .attrs({ fill: red, height, y: 0 });

    const aRect = svg.append('rect')
        .attrs({ fill: amber, height, y: 0 });

    const gRect = svg.append('rect')
        .attrs({ fill: green, height, y: 0 });

    const zRect = svg.append('rect')
        .attrs({ fill: grey, height, y: 0 });

    const update = (scores, range = [0, 0]) => {
        const xScale = scaleLinear()
            .domain([0, range[1]])
            .range([0, width]);

        const r = { x: 0, width : xScale(scores.R || 0) };
        const a = { x: 0 + r.width, width : xScale(scores.A || 0) };
        const g = { x: a.x + a.width, width: xScale(scores.G || 0) };
        const z = { x: g.x + g.width, width : xScale(scores.Z || 0) };

        rRect.attrs(r);
        aRect.attrs(a);
        gRect.attrs(g);
        zRect.attrs(z);
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
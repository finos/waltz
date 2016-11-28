import {select} from 'd3-selection';
import 'd3-selection-multi';

import { authoritativeSourceColorScale } from '../../common/colors';


const radius = 8;
const padding = 3;

function link(scope, elem) {

    const data = ['PRIMARY', 'SECONDARY'];
    const svg = select(elem[0]);

    svg.selectAll('circle')
        .data(data)
        .enter()
        .append('circle')
        .attrs({
            cx: (d, i) => i * (radius * 2 + padding * 2) + radius + padding / 2,
            cy: radius + padding / 2,
            r: radius
        });

    scope.$watch('value', (value) => {
        svg.selectAll('circle')
            .data(data)
            .attrs({
                fill: (d) => ( d === value) ? authoritativeSourceColorScale(d) : '#eee',
                stroke: (d) => ( d === value) ? authoritativeSourceColorScale(d).darker() : '#ddd'
            });
    });
}


export default () => ({
    restrict: 'E',
    replace: true,
    template: `<svg width="${ 3 * ( radius * 2 + padding * 2)}" height="${radius * 2 + padding}"></svg>`,
    scope: { value: '@'},
    link
});

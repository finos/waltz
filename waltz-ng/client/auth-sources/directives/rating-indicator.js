/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017 Waltz open source project
 * See README.md for more information
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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

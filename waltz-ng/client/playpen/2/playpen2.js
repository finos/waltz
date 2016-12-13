/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016  Khartec Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import {event, selectAll, select} from 'd3-selection'

const svg = `
     
`;


function calculateBoundingRect(clientRect, referenceElement) {
    const hPosX = referenceElement.getBoundingClientRect().left;
    const hPosY = referenceElement.getBoundingClientRect().top;

    return {
        x: clientRect.left - hPosX,
        y: clientRect.top - hPosY,
        width: clientRect.width,
        height: clientRect.height
    };
}


function controller(notification,
                    $element) {

    const vm = Object.assign(this, {});

    const highlight = (elem) => {

        const dimensions = calculateBoundingRect(
            elem.getBoundingClientRect(),
            document.getElementById('graphic'));

        select('#highlighter')
            .attr('x', dimensions.x)
            .attr('y', dimensions.y)
            .attr('width', dimensions.width)
            .attr('height', dimensions.height);
    };

    vm.annotate = () => {

        selectAll('g')
            .each(function(g) {
                const dimensions = calculateBoundingRect(
                    select(this)
                        .node()
                        .getBoundingClientRect(),
                    document
                        .getElementById('graphic'));

                select(this)
                    .insert('rect', ":first-child")
                    .attr('visibility', 'hidden')
                    .style('pointer-events', 'fill')
                    .attr('x', dimensions.x)
                    .attr('y', dimensions.y)
                    .attr('width', dimensions.width)
                    .attr('height', dimensions.height);
            });

        document.getElementById('graphic').onmousemove =
            e => {
                const mx = e.clientX;
                const my = e.clientY;
                const elementMouseIsOver = document.elementFromPoint(mx, my);
                highlight(elementMouseIsOver);
            };
    }


}



controller.$inject = [
    'Notification',
    '$element'
];


const view = {
    template: require('./playpen2.html'),
    controller,
    controllerAs: 'ctrl',
    bindToController: true,
    scope: {}
};


export default view;

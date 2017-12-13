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

import {event, select} from 'd3-selection';


const bindings = {
    svg: '<'
};



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


function controller($element) {
    const vm = this;

    const highlight = (elem) => {
        select(elem).attr('stroke', 'red').attr('stroke-width', 1.5);
    };

    const highlighter = () => {
        const mx = event.clientX;
        const my = event.clientY;
        const elementMouseIsOver = document.elementFromPoint(mx, my);
        highlight(elementMouseIsOver);
    };

    const annotate = (selection) => {
        selection
            .selectAll('g')
            .each(function(g) {
                const dimensions =
                    select(this)
                        .node()

                select(this)
                    .insert('rect', ":first-child")
                    .attr('visibility', 'hidden')
                    .style('pointer-events', 'fill')
                    .attr('x', dimensions.x)
                    .attr('y', dimensions.y)
                    .attr('width', dimensions.width)
                    .attr('height', dimensions.height);
            });
    };



    vm.$onChanges = () => {
        const diagram = select($element[0])
            .html(vm.svg)
            .on('mousemove.highlighter', highlighter);

        diagram
            .call(annotate);

        diagram
            .select('svg')
            .append('rect')
            .attr('id', 'highlighter')
            .attr('fill', 'none')
            .attr('stroke', 'red');
    };

}


controller.$inject = ['$element'];


const component = {
    template: '<div></div>',
    bindings,
    controller
};

export default component;
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
                    svgDiagramStore,
                    $element) {

    const vm = Object.assign(this, {});

    svgDiagramStore
        .findByGroup('ORG_UNIT')
        .then(x => {
            const diagram = x[0]
            document
                .getElementById('holder')
                .innerHTML = diagram.svg;

            select('#holder svg')
                .append('rect')
                .attr('id', 'highlighter')
                .attr('stroke', 'red');

            vm.annotate();
            return x;
        });

    const highlight = (elem) => {

        console.log(elem)
        // const dimensions = calculateBoundingRect(
        //     elem.getBoundingClientRect(),
        //     document.getElementById('holder'));
        //
        // select('#highlighter')
        //     .attr('x', dimensions.x)
        //     .attr('y', dimensions.y)
        //     .attr('width', dimensions.width)
        //     .attr('height', dimensions.height);
    };

    vm.annotate = () => {



        document.getElementById('holder').onmousemove =
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
    'SvgDiagramStore',
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

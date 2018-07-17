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

import {scaleLinear} from 'd3-scale';
import {max, min} from 'd3-array';
import template from './flow-cloud-diagram.html';


const BINDINGS = {
    stats: '<'
};


function updateDiagram(rootElem, stats) {
    const { appCounts, flowCounts } = stats;

    updateAppCounts(rootElem, appCounts);
    updateRatios(rootElem, appCounts, flowCounts);
    updateHoverHelp(rootElem, appCounts, flowCounts);
    updateArrows(rootElem, appCounts);
}


function updateArrows(rootElem, apps) {

    const counts = [
        apps.inbound,
        apps.outbound ];

    const arrowScale = scaleLinear()
        .domain([
            min(counts),
            max(counts)
        ])
        .range([0.85, 1.12]);

    resizeArrow(rootElem, 'in-arrow', arrowScale(apps.inbound));
    resizeArrow(rootElem, 'out-arrow', arrowScale(apps.outbound));
}


function updateHoverHelp(rootElem, apps, flows) {
    setHover(
        rootElem,
        'intra-ratio',
        `(#unique app connections / #apps in group) \n => ${ flows.intra } / ${ apps.intra }`);

    if (flows.inbound > 0) {
        setHover(rootElem,
            'in-ratio',
            `(#unique app connections / #apps in group) \n => ${ flows.inbound } / ${ apps.intra }`);
    }

    if (flows.outbound > 0) {
        setHover(rootElem,
            'out-ratio',
            `(#unique app connections / #apps in group) \n => ${ flows.outbound } / ${ apps.intra }`);
    }
}


function calculateRatios(apps, flows) {
    if (! apps.intra) return {
        inbound: 'n/a',
        outbound: 'n/a',
        intra: 'n/a'
    };

    return {
        inbound: (flows.inbound / apps.intra).toFixed(2),
        outbound: (flows.outbound / apps.intra).toFixed(2),
        intra: (flows.intra / apps.intra).toFixed(2)
    }
}


function updateRatios(rootElem, apps, flows) {
    const ratios = calculateRatios(apps, flows);

    setText(rootElem, 'intra-ratio', `Intra-app connectivity ratio: ${ ratios.intra }`);

    if (flows.inbound > 0) {
        setText(rootElem, 'in-ratio', `Connectivity ratio: ${ ratios.inbound }`);
    }
    if (flows.outbound > 0) {
        setText(rootElem, 'out-ratio', `Connectivity ratio: ${ ratios.outbound }`);
    }
}


function updateAppCounts(rootElem, apps) {
    setText(rootElem, 'intra-count', `${ apps.intra } app/s`);
    setText(rootElem, 'in-count', `${ apps.inbound } sending app/s`);
    setText(rootElem, 'out-count', `${ apps.outbound } receiving app/s`);
}


function setText(root, id, text = '') {
    const elem = root.getElementById(id).getElementsByTagName("text")[0];
    if (elem) {
        elem.textContent = text;
    } else {
        console.log("FlowCloudDiagram: Could not find element with id: " + id);
    }
}


function setHover(root, id, text = '') {
    const elem = root.getElementById(id);
    if (elem) {
        const title =  document.createElementNS("http://www.w3.org/2000/svg","title");
        title.textContent = text;
        elem.appendChild(title);
    } else {
        console.log("FlowCloudDiagram: Could not find element with id: " + id);
    }
}


function resizeArrow(root, id, scale) {
    const arrow = root.getElementById(id);
    const sx = scale;
    const sy = scale;
    const cx = 125;
    const cy = 100;

    const matrix = `matrix(${sx}, 0, 0, ${sy}, ${cx - sx * cx}, ${cy - sy * cy} )`;

    arrow.setAttribute('transform', matrix);
}


function controller($scope,
                    $element) {

    const rootElem = $element[0];

    $scope.$watch(
        'ctrl.stats',
        stats => {
            if (stats) updateDiagram(rootElem, stats)
        });
}

controller.$inject = [
    '$scope',
    '$element'
];


const directive = {
    restrict: 'E',
    replace: true,
    template,
    scope: {},
    controller,
    controllerAs: 'ctrl',
    bindToController: BINDINGS
};

export default () => directive;

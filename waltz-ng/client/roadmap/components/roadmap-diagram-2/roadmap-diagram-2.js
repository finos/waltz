/*
 * Waltz - Enterprise Architecture
 *  Copyright (C) 2016, 2017 Waltz open source project
 *  See README.md for more information
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import {initialiseData} from "../../../common";
import template from "./roadmap-diagram-2.html";
import {select} from "d3-selection";
import {createGroupElements, truncateText} from "../../../common/d3-utils";


const bindings = {
};


const STYLES = {
    node: "wrd-node",
    nodeTitle: "wrd-node-title",
    nodeExternalId: "wrd-node-external-id",
    nodeCell: "wrd-node-cell"
};


const initialState = {};


function setupGroupElements($element) {
    const svg = select($element.find("svg")[0]);
    const definitions = [
    ];
    return Object.assign({}, { svg }, createGroupElements(svg, definitions));
}


function draw(groups) {

    const datum = {
        node: {name: "Test Application with a long name", externalId: "1234-1", description: "about test app"},
        change: {current: {rating: "R"}, future: {rating: "G"}},
        changeInitiative: {name: "Change the bank", externalId: "INV1234", description: "Make some changes"}
    };

    const data  = [
        datum,
        datum,
        datum,
        datum
    ];

    // centerpoint
    groups.svg.append("circle").attr("fill", "red").attr("r", 5).attr("cx", 0).attr("cy", 0);

    groups.svg
        .selectAll(`.${STYLES.node}`)
        .data(data)
        .enter()
        .append("g")
        .attr("transform", (d, i) => `translate(${(nodeDimensions.width + 20) * (i)} 100)`)
        .classed(STYLES.node, true)
        .call(drawUnit)
}


const nodeDimensions = {
    width: 140,
    height: 60,  // 3 * sectionHeight
    section: {
        height: 20
    },
    text: {
        dy: 14,
        dx: 4,
        fontSize: 12
    }
};


function drawUnit(selection) {
    selection
        .append("rect")
        .classed(STYLES.nodeCell, true)
        .attr("width", nodeDimensions.width)
        .attr("height", nodeDimensions.height)
        .attr("fill", "#ccc")
        .attr("stroke", "#888");

    selection
        .call(drawUnitTitle);

    selection
        .call(drawUnitExternalId);
}


function drawUnitExternalId(selection) {
    selection
        .append("rect")
        .classed(STYLES.nodeExternalId, true)
        .attr("width", nodeDimensions.width / 2)
        .attr("height", nodeDimensions.section.height)
        .attr("x", nodeDimensions.width / 2)
        .attr("y", nodeDimensions.section.height)
        .attr("fill", "#ddd")
        .attr("stroke", "#888");

    selection
        .append("text")
        .classed(STYLES.nodeExternalId, true)
        .attr("x", nodeDimensions.width / 2)
        .attr("y", nodeDimensions.section.height)
        .text(d => d.node.externalId)
        .attr("dy", nodeDimensions.text.dy)
        .attr("dx", nodeDimensions.text.dx)
        .attr("font-size", nodeDimensions.text.fontSize - 2)
        .call(truncateText, nodeDimensions.width - (2 * nodeDimensions.text.dx));
}


function drawUnitTitle(selection) {
    selection
        .append("rect")
        .classed(STYLES.nodeTitle, true)
        .attr("width", nodeDimensions.width)
        .attr("height", nodeDimensions.section.height)
        .attr("fill", "#eee")
        .attr("stroke", "#888");

    selection
        .append("text")
        .text(d => d.node.name)
        .classed(STYLES.nodeTitle, true)
        .attr("dy", nodeDimensions.text.dy)
        .attr("dx", nodeDimensions.text.dx)
        .attr("font-size", nodeDimensions.text.fontSize)
        .call(truncateText, nodeDimensions.width - (2 * nodeDimensions.text.dx));
}


function controller($element) {
    const vm = initialiseData(this, initialState);

    let svgGroups = null;

    function redraw() {
        if (svgGroups) {
            draw(svgGroups);
        }
    }

    vm.$onInit = () => {
        svgGroups = setupGroupElements($element);
        redraw();
    };

    vm.$onChanges = (changes) => {
        console.log("roadmap-diagram-2 changes - parentEntityRef: ", vm.parentEntityRef);
        redraw();
    };

}


controller.$inject = ["$element"];


const component = {
    template,
    bindings,
    controller
};


export default {
    component,
    id: "waltzRoadmapDiagram2"
};

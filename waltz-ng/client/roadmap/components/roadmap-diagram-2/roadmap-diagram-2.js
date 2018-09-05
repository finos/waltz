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
import {CORE_API} from "../../../common/services/core-api-utils";
import {mkRatingSchemeColorScale} from "../../../common/colors";
import {drawUnit, NODE_STYLES, NODE_DIMENSIONS} from "./roadmap-diagram-node-utils";


const bindings = {
};


const initialState = {};


function setupGroupElements($element) {
    const svg = select($element.find("svg")[0]);
    const definitions = [
    ];
    return Object.assign({}, { svg }, createGroupElements(svg, definitions));
}


function draw(groups, ratingColorScheme) {

    const datum = {
        node: {name: "Test Application with a long name", externalId: "1234-1", description: "about test app"},
        change: {current: {rating: "R"}, future: {rating: "G"}},
        changeInitiative: {name: "Change the bank", externalId: "INV1234", description: "Make some changes"}
    };
    const datum2 = {
        node: {name: "Another application", externalId: "5678-1", description: "about test app"},
        change: {current: {rating: "Z"}, future: {rating: "G"}},
        changeInitiative: null
    };

    const datum3 = {
        node: {name: "Waltz", externalId: "2468-1", description: "about test app"},
        change: {current: {rating: "A"}, future: {rating: "G"}},
        changeInitiative: null
    };
    const datum4 = {
        node: {name: "FDW", externalId: "8529-1", description: "about test app"},
        change: {current: {rating: "G"}, future: {rating: "X"}},
        changeInitiative:{name: "Change the bank", externalId: "INV6547", description: "Make some changes"}
    };
    const datum5 = {
        node: {name: "TDH", externalId: "8529-1", description: "about test app"},
        change: {current: {rating: "G"}, future: {rating: "A"}},
        changeInitiative:{name: "Change the bank", externalId: "INV6547", description: "Make some changes"}
    };

    const datum6 = {
        node: {name: "Sales Broker", externalId: "8529-1", description: "about test app"},
        change: {current: {rating: "G"}, future: {rating: "G"}},
        changeInitiative:{name: "Change the bank", externalId: "INV6547", description: "Make some changes"}
    };

    const data  = [
        datum,
        datum2,
        datum3,
        datum4,
        datum5,
        datum6
    ];

    const gridData = gridLayout(data, { cols: 3});



    // centerpoint
    groups.svg.append("circle").attr("fill", "red").attr("r", 5).attr("cx", 0).attr("cy", 0);

    groups.svg
        .selectAll(`.${NODE_STYLES.node}`)
        .data(data)
        .enter()
        .append("g")
        .attr("transform", (d, i) => `translate(${(NODE_DIMENSIONS.width + 20) * (i)} 100)`)
        .classed(NODE_STYLES.node, true)
        .call(drawUnit, ratingColorScheme)
}

function gridLayout(data = [], options = { cols: 3}) {
    return _.map(data, d => {
        return {
            data: d,
            x:1,
            y:1
        };
    });
}



function controller($element, serviceBroker) {
    const vm = initialiseData(this, initialState);

    let svgGroups = null;

    function redraw() {
        console.log('redraw', vm)
        const colorScheme = mkRatingSchemeColorScale(_.find(vm.ratingSchemes, { id: 1 }));
        if (svgGroups && colorScheme) {
            draw(svgGroups, colorScheme);
        }
    }

    vm.$onInit = () => {
        svgGroups = setupGroupElements($element);
        serviceBroker
            .loadAppData(CORE_API.RatingSchemeStore.findAll)
            .then(r => vm.ratingSchemes = r.data)
            .then(redraw);
    };

    vm.$onChanges = (changes) => {
        console.log("roadmap-diagram-2 changes - parentEntityRef: ", vm.parentEntityRef);
        redraw();
    };

}


controller.$inject = ["$element", "ServiceBroker"];


const component = {
    template,
    bindings,
    controller
};


export default {
    component,
    id: "waltzRoadmapDiagram2"
};

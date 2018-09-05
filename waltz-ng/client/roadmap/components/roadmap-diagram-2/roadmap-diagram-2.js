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


function draw(data, groups, ratingColorScheme) {

    const gridData = gridLayout(data, { cols: 2 });

    // marker
    //groups.svg.append("circle").attr("fill", "red").attr("r", 5).attr("cx", 0).attr("cy", 0);

    const grid = groups.svg
        .selectAll(`.${NODE_STYLES.node}`)
        .data(gridData, d => d.id);

    const newCells = grid.enter()
        .append("g")
        .classed(NODE_STYLES.node, true);

    grid.exit()
        .remove();

    const cellPadding = 10;

    grid.merge(newCells)
        .attr("transform", d => {
            const dy = cellPadding + (NODE_DIMENSIONS.height + cellPadding) * d.layout.y;
            const dx = cellPadding + (NODE_DIMENSIONS.width + cellPadding) * d.layout.x;
            return `translate(${dx} ${dy})`;
        })
        .call(drawUnit, ratingColorScheme);
}


function gridLayout(data = [], options = { cols: 3 }) {
    return _.map(
        data,
        (d, idx) => {
            const layout = {
                x: idx % options.cols,
                y: Math.floor(idx / options.cols)
            };
            return Object.assign({}, d, { layout });
        });
}



function controller($element, serviceBroker) {
    const vm = initialiseData(this, initialState);


    const datum = {
        id: 1,
        node: {name: "Test Application with a long name", externalId: "1234-1", description: "about test app"},
        change: {current: {rating: "R"}, future: {rating: "G"}},
        changeInitiative: {name: "Change the bank", externalId: "INV1234", description: "Make some changes"}
    };
    const datum2 = {
        id: 2,
        node: {name: "Another application", externalId: "5678-1", description: "about test app"},
        change: {current: {rating: "Z"}, future: {rating: "G"}},
        changeInitiative: null
    };

    const datum3 = {
        id: 3,
        node: {name: "Waltz", externalId: "2468-1", description: "about test app"},
        change: {current: {rating: "A"}, future: {rating: "G"}},
        changeInitiative: null
    };
    const datum4 = {
        id: 4,
        node: {name: "FDW", externalId: "8529-1", description: "about test app"},
        change: {current: {rating: "G"}, future: {rating: "X"}},
        changeInitiative:{name: "Change the bank", externalId: "INV6547", description: "Make some changes"}
    };
    const datum5 = {
        id: 5,
        node: {name: "TDH", externalId: "8529-1", description: "about test app"},
        change: {current: {rating: "G"}, future: {rating: "A"}},
        changeInitiative:{name: "Change the bank", externalId: "INV6547", description: "Make some changes"}
    };

    const datum6 = {
        id: 6,
        node: {name: "Sales Broker", externalId: "8529-1", description: "about test app"},
        change: {current: {rating: "G"}, future: {rating: "G"}},
        changeInitiative:{name: "Change the bank", externalId: "INV6547", description: "Make some changes"}
    };

    const datum7 = {
        id: 7,
        node: {name: "Risk Calculator", externalId: "9977-1", description: "about test app"},
        change: {current: {rating: "G"}, future: {rating: "G"}},
        changeInitiative:{name: "Change the bank", externalId: "INV6547", description: "Make some changes"}
    };

    vm.data  = [
        datum,
        datum2,
        datum3,
        datum4,
        datum5,
        datum6,
        datum7
    ];

    let svgGroups = null;

    function redraw() {
        console.log('redraw', vm)
        const colorScheme = mkRatingSchemeColorScale(_.find(vm.ratingSchemes, { id: 1 }));
        if (svgGroups && colorScheme) {
            draw(vm.data, svgGroups, colorScheme);
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

    vm.onRemoveCell = (idx) => {
        vm.data = _.filter(vm.data, (d, i) => i != idx);
        redraw();
    };

    vm.onAddCell = () => {
        const t = _.random(0, 10000000);
        const rs = ["R", "A", "G", "Z", "X"];
        const mkRating = () => rs[_.random(0, rs.length)]
        const cell = {
            id: t,
            node: {name: `App ${t}`, externalId: `${t}-1`, description: "about test app"},
            change: {current: {rating: mkRating()}, future: {rating: mkRating()}},
            changeInitiative:{name: "Change the bank", externalId: "INV6547", description: "Make some changes"}
        };
        vm.data = _.concat(vm.data, [ cell ]);
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

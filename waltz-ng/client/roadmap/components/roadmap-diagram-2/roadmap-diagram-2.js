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
import {createGroupElements} from "../../../common/d3-utils";
import {CORE_API} from "../../../common/services/core-api-utils";
import {mkRatingSchemeColorScale} from "../../../common/colors";
import {drawNodeGrid, gridLayout} from "./roadmap-diagram-node-grid-utils";
import {mkRandomNode, mkRandomNodes} from "./roadmap-diagram-data-utils";


const bindings = {
};


const initialState = {
    numCols: 3
};


function setupGroupElements($element) {
    const svg = select($element.find("svg")[0]);
    const definitions = [
    ];
    return Object.assign({}, { svg }, createGroupElements(svg, definitions));
}


function draw(gridData, groups, ratingColorScheme) {
    groups.svg
        .call(drawNodeGrid, gridData, ratingColorScheme);
}



function controller($element, serviceBroker) {
    const vm = initialiseData(this, initialState);

    vm.data = mkRandomNodes();

    let svgGroups = null;

    function redraw() {
        console.log("redraw", vm);
        const colorScheme = mkRatingSchemeColorScale(_.find(vm.ratingSchemes, { id: 1 }));
        if (svgGroups && colorScheme) {
            const gridData = gridLayout(vm.data, { cols: vm.numCols });
            draw(gridData, svgGroups, colorScheme);
        }
    }

    vm.$onInit = () => {
        svgGroups = setupGroupElements($element);
        serviceBroker
            .loadAppData(CORE_API.RatingSchemeStore.findAll)
            .then(r => vm.ratingSchemes = r.data)
            .then(redraw);
    };

    vm.$onChanges = () => {
        console.log("roadmap-diagram-2 changes - parentEntityRef: ", vm.parentEntityRef);
        redraw();
    };

    vm.onRemoveCell = (idx) => {
        vm.data = _.filter(vm.data, (d, i) => i !== idx);
        redraw();
    };

    vm.onAddCell = () => {
        const cell = mkRandomNode();
        vm.data = _.concat(vm.data, [ cell ]);
        redraw();
    };

    vm.onAddCol = () => { vm.numCols = vm.numCols + 1; redraw(); };
    vm.onRemoveCol = () => { vm.numCols = vm.numCols - 1; redraw(); };
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

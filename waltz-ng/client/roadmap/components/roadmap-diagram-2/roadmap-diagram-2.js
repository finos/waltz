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

import {initialiseData, isEmpty} from "../../../common";
import template from "./roadmap-diagram-2.html";
import {select} from "d3-selection";
import {createGroupElements, responsivefy} from "../../../common/d3-utils";
import {CORE_API} from "../../../common/services/core-api-utils";
import {mkRatingSchemeColorScale} from "../../../common/colors";
import {filterData, mkRandomNode, mkRandomRowData} from "./roadmap-diagram-data-utils";
import {drawRow, rowLayout} from "./roadmap-diagram-row-utils";

const bindings = {
};


const initialState = {
    numCols: 3
};


function setupGroupElements($element) {
    const svg = select($element.find("svg")[0]);
    const definitions = [
        {
            name: "holder",
            children: [
                { name: "grid", children: [ ] },
                { name: "columnAxis", children: [ ] },
                { name: "rowAxis", children: [ ]  }
            ]
        }
    ];
    return Object.assign({}, { svg }, createGroupElements(svg, definitions));
}


function draw(data, holder, colorScheme) {
    console.log("draw");
    return drawRow(data, holder, colorScheme);
}


function controller($element, serviceBroker) {
    const vm = initialiseData(this, initialState);

    vm.data = mkRandomRowData(4);

    let svgGroups = null;
    let destructorFn = null;

    function redraw() {
        console.log("redraw", vm);
        const colorScheme = mkRatingSchemeColorScale(_.find(vm.ratingSchemes, { id: 1 }));
        if (svgGroups && colorScheme) {
            const data = filterData(vm.data, vm.qry);
            const dataWithLayout = rowLayout(data);
            draw(dataWithLayout, svgGroups.grid, colorScheme);
        }
    }

    vm.$onInit = () => {
        svgGroups = setupGroupElements($element);
        destructorFn = responsivefy(svgGroups.svg);
        serviceBroker
            .loadAppData(CORE_API.RatingSchemeStore.findAll)
            .then(r => vm.ratingSchemes = r.data)
            .then(redraw);
    };

    vm.$onDestroy = () => {
        if (destructorFn) {
            destructorFn();
        }
    };

    vm.doSearch = () => {
        redraw();
    };

    vm.$onChanges = () => {
        console.log("roadmap-diagram-2 changes - parentEntityRef: ", vm.parentEntityRef);
        redraw();
    };

    // -- INTERACT --

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

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
import {createGroupElements, responsivefy} from "../../../common/d3-utils";
import {CORE_API} from "../../../common/services/core-api-utils";
import {mkRatingSchemeColorScale} from "../../../common/colors";
import {filterData, mkRandomMeasurable, mkRandomRowData} from "./roadmap-diagram-data-utils";
import {setupZoom} from "./roadmap-diagram-utils";
import _ from "lodash";
import {drawGrid, gridLayout} from "./roadmap-diagram-grid-utils";
import {columnAxisHeight, drawAxis, rowAxisWidth} from "./roadmap-diagram-axis-utils";


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
                {
                    name: "gridHolder",
                    attrs: {
                        "clip-path": "url(#grid-clip)",
                        "transform": `translate(${rowAxisWidth} ${columnAxisHeight})`
                    },
                    children: [ { name: "gridContent" } ]
                }, {
                    name: "columnAxisHolder",
                    attrs: {
                        "clip-path": "url(#column-clip)",
                        "transform": `translate(${rowAxisWidth} 0)`
                    },
                    children: [ { name: "columnAxisContent" }]
                }, {
                    name: "rowAxisHolder",
                    attrs: {
                        "clip-path": "url(#row-clip)",
                        "transform": `translate(0 ${columnAxisHeight})`
                    },
                    children: [ { name: "rowAxisContent" } ]
                }
            ]
        }
    ];
    return Object.assign({}, { svg }, createGroupElements(svg, definitions));
}


function draw(dataWithLayout, svgGroups, colorScheme) {
    console.log("draw", dataWithLayout);
    drawGrid(svgGroups.gridContent, dataWithLayout, colorScheme);
    drawAxis(svgGroups.columnAxisContent, svgGroups.rowAxisContent, dataWithLayout);
}


function controller($element, serviceBroker) {
    const vm = initialiseData(this, initialState);
    let svgGroups = null;
    let destructorFn = null;

    const rowCount = 8;
    const colCount = 12;

    vm.data = _.times(rowCount, () => mkRandomRowData(colCount));
    vm.columnHeaders = _.times(colCount, i => mkRandomMeasurable(i, "col"));
    vm.rowHeaders = _.times(rowCount, i => mkRandomMeasurable(i, "row"));

    function redraw() {
        const colorScheme = mkRatingSchemeColorScale(_.find(vm.ratingSchemes, { id: 1 }));
        if (svgGroups && colorScheme) {
            const filteredData = filterData(vm.data, vm.qry);
            const dataWithLayout = gridLayout(filteredData, vm.columnHeaders, vm.rowHeaders, { cols: 4 });
            draw(dataWithLayout, svgGroups, colorScheme);
        }
    }

    vm.$onInit = () => {
        svgGroups = setupGroupElements($element);

        setupZoom(svgGroups);
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

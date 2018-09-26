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

import {initialiseData} from "../../../common/index";
import template from "./scenario-diagram.html";
import {select} from "d3-selection";
import {createGroupElements, responsivefy} from "../../../common/d3-utils";
import {CORE_API} from "../../../common/services/core-api-utils";
import {mkRatingSchemeColorScale} from "../../../common/colors";
import {filterData} from "./scenario-diagram-data-utils";
import {setupZoom} from "./scenario-diagram-utils";
import _ from "lodash";
import {drawGrid, gridLayout} from "./scenario-diagram-grid-utils";
import {columnAxisHeight, drawAxis, rowAxisWidth} from "./scenario-diagram-axis-utils";


const bindings = {
    rowData: "<",
    rowHeadings: "<",
    columnHeadings: "<",
    handlers: "<?"
};


const defaultHandlers = {
    onNodeClick: (d) => console.log("WRD: NodeClick", d),
    onNodeGridClick: (d) => console.log("WRD: NodeGridClick", d)
};


const initialState = {
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


function draw(dataWithLayout, svgGroups, options) {
    console.log("WRD: draw", { dataWithLayout, options });
    drawGrid(svgGroups.gridContent, dataWithLayout, options);
    drawAxis(svgGroups.columnAxisContent, svgGroups.rowAxisContent, dataWithLayout);
}


function controller($element, serviceBroker) {
    const vm = initialiseData(this, initialState);
    let svgGroups = null;
    let destructorFn = null;

    function redraw() {
        const colorScale = mkRatingSchemeColorScale(_.find(vm.ratingSchemes, { id: 1 }));
        if (svgGroups && colorScale) {
            const filteredData = filterData(vm.rowData, vm.qry);
            const layoutOptions = { cols: 3 };
            const dataWithLayout = gridLayout(
                filteredData,
                vm.columnHeadings,
                vm.rowHeadings,
                layoutOptions);

            const drawingOptions = {
                colorScale,
                handlers: vm.handlers
            };
            draw(dataWithLayout, svgGroups, drawingOptions);
        }
    }

    vm.$onInit = () => {
        vm.handlers = Object.assign({}, defaultHandlers, vm.handlers);
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

    vm.$onChanges = (c) => {
        console.log("roadmap-diagram changes: ", c);
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
    id: "waltzScenarioDiagram"
};

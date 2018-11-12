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

import {initialiseData, isEmpty} from "../../../common/index";
import {select} from "d3-selection";
import {createGroupElements, responsivefy} from "../../../common/d3-utils";
import {CORE_API} from "../../../common/services/core-api-utils";
import {mkRatingSchemeColorScale} from "../../../common/colors";
import {filterData} from "./scenario-diagram-data-utils";
import {removeZoom, resetZoom, setupZoom} from "./scenario-diagram-utils";
import _ from "lodash";
import {drawGrid, gridLayout} from "./scenario-diagram-grid-utils";
import {columnAxisHeight, drawAxis, rowAxisWidth} from "./scenario-diagram-axis-utils";
import {invokeFunction} from "../../../common";


import template from "./scenario-diagram.html";


const bindings = {
    ratingSchemeId: "<",
    rowData: "<",
    rowHeadings: "<",
    columnHeadings: "<",
    handlers: "<?",
    hiddenAxes: "<?",
    layoutOptions: "<?",

    onUnhideAxis: "<?",
    onUnhideAllAxes: "<?",
};


const defaultHandlers = {
    onNodeClick: (d) => console.log("WSD: NodeClick", d),
    onNodeGridClick: (d) => console.log("WSD: NodeGridClick", d),
};


const SORT_COLUMN = {
    NAME: "name",
    RATING: "rating"
};


const initialState = {
    hiddenAxes: [],
    panAndZoomEnabled: false,
    sortBy: SORT_COLUMN.NAME,
    visibility: {
        diagramControls: false
    },
    ratingScheme: null,
    layoutOptions: {
        defaultColMaxWidth: 2,
        sortFn: d => d.node.name
    },

    onUnhideAxis: (axis) => console.log("WSD: UnhideAxis", axis),
    onUnhideAllAxes: (axis) => console.log("WSD: UnhideAllAxes", axis)
};


function setupGroupElements($element) {
    const svg = select($element.find("svg")[0]);
    const definitions = [
        {
            name: "holder",
            children: [
                {
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
                }, {
                    name: "gridHolder",
                    attrs: {
                        "clip-path": "url(#grid-clip)",
                        "transform": `translate(${rowAxisWidth} ${columnAxisHeight})`
                    },
                    children: [
                        {
                            name: "gridContent",
                            attrs: { "transform": "translate(1, 1)" }
                        }
                    ]
                },
            ]
        }
    ];
    return Object.assign({}, { svg }, createGroupElements(svg, definitions));
}


function draw(dataWithLayout, svgGroups, options) {
    // console.log("WSD: draw", { dataWithLayout, options });
    drawAxis(svgGroups.columnAxisContent, svgGroups.rowAxisContent, dataWithLayout, options);
    drawGrid(svgGroups.gridContent, dataWithLayout, options);
}


function controller($element, $timeout, serviceBroker) {
    const vm = initialiseData(this, initialState);
    let svgGroups = null;
    let destructorFn = null;

    const enablePanAndZoomAction = { title: "Disable pan and zoom", action: (elm, d) => $timeout(() => vm.disablePanAndZoom()) };
    const disablePanAndZoomAction = { title: "Enable pan and zoom", action: (elm, d) => $timeout(() => vm.enablePanAndZoom()) };
    const resetViewAction = { title: "Reset view", action: (elm, d) => $timeout(() => vm.resetPanAndZoom()) };
    const menuDivider = { divider: "pan"};

    function enrichHandlers(handlers) {
        const newContextMenus =  _.reduce(
            handlers.contextMenus,
            (acc, fn, k) => {
                acc[k] = (data, elm, idx) => {
                    const panAction = vm.panAndZoomEnabled
                        ? (enablePanAndZoomAction)
                        : (disablePanAndZoomAction);
                    const newItems = [ panAction, resetViewAction ];

                    const existingItems = fn(data, elm, idx);

                    return isEmpty(existingItems)
                        ? newItems
                        : _.uniq(_.concat(existingItems, [ menuDivider ] , newItems) );
                };
                return acc;
            },
            {});
        return Object.assign(handlers, { contextMenus: newContextMenus });
    }

    function redraw() {
        vm.ratingScheme = _.find(vm.ratingSchemes, { id: vm.ratingSchemeId });
        const colorScale = mkRatingSchemeColorScale(vm.ratingScheme);

        if (svgGroups && colorScale && vm.rowData) {
            const filteredData = filterData(vm.rowData, vm.qry);
            const ratingsByCode = _.keyBy(vm.ratingScheme.ratings, "rating");

            switch (vm.sortBy) {
                case SORT_COLUMN.RATING:
                    vm.layoutOptions.sortFn = d => ratingsByCode[d.state.rating] ? ratingsByCode[d.state.rating].position : d.node.name;
                    break;
                case SORT_COLUMN.NAME:
                    vm.layoutOptions.sortFn = d => d.node.name;
                    break;
            }

            const dataWithLayout = gridLayout(
                filteredData,
                vm.columnHeadings,
                vm.rowHeadings,
                vm.layoutOptions);

            const drawingOptions = {
                colorScale,
                handlers: enrichHandlers(vm.handlers)
            };
            draw(dataWithLayout, svgGroups, drawingOptions);
        }
    }

    vm.$onInit = () => {
        vm.handlers = Object.assign({}, defaultHandlers, vm.handlers);
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


    // -- interact --

    vm.doSearch = () => {
        redraw();
    };

    vm.$onChanges = () => {
        redraw();
    };

    vm.enablePanAndZoom = () => {
        vm.panAndZoomEnabled = true;
        setupZoom(svgGroups);
    };

    vm.disablePanAndZoom = () => {
        vm.panAndZoomEnabled = false;
        removeZoom(svgGroups);
    };

    vm.resetPanAndZoom = () => {
        resetZoom(svgGroups);
    };

    vm.changeSort = (column) => {
        vm.sortBy = column;
        redraw();
    };

    vm.showAxis = (axis) => {
        invokeFunction(vm.onUnhideAxis, axis);
    };

    vm.showAllAxes = () => {
        invokeFunction(vm.onUnhideAllAxes);
    };
}


controller.$inject = [
    "$element",
    "$timeout",
    "ServiceBroker"
];


const component = {
    template,
    bindings,
    controller
};


export default {
    component,
    id: "waltzScenarioDiagram"
};

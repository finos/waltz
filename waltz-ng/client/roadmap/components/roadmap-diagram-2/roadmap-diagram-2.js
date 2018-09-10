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
import {filterData, mkRandomRowData} from "./roadmap-diagram-data-utils";
import {setupZoom} from "./roadmap-diagram-utils";
import _ from "lodash";
import {drawGrid, gridLayout} from "./roadmap-diagram-grid-utils";


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
                { name: "grid", children: [ { name: "gridContent" } ] },
                { name: "columnAxis", children: [ { name: "columnContent" }] },
                { name: "rowAxis", children: [ { name: "rowContent" } ]  }
            ]
        }
    ];
    return Object.assign({}, { svg }, createGroupElements(svg, definitions));
}


function draw(dataWithLayout, holder, colorScheme) {
    console.log("draw", dataWithLayout);
    drawGrid(holder, dataWithLayout, colorScheme);
}


function controller($element, serviceBroker) {
    const vm = initialiseData(this, initialState);

    vm.data = _.times(12, () => mkRandomRowData(8));

    let svgGroups = null;
    let destructorFn = null;

    function redraw() {
        console.log("redraw", vm);
        const colorScheme = mkRatingSchemeColorScale(_.find(vm.ratingSchemes, { id: 1 }));
        if (svgGroups && colorScheme) {
            const filteredData = filterData(vm.data, vm.qry);
            const dataWithLayout = gridLayout(filteredData, { cols: 4 });
            draw(dataWithLayout, svgGroups.gridContent, colorScheme);
        }
    }

    vm.$onInit = () => {
        svgGroups = setupGroupElements($element);
        svgGroups
            .grid
            .attr("clip-path", "url(#grid-clip)")
            .attr("transform", "translate(150 50)");
        svgGroups
            .columnAxis
            .attr("clip-path", "url(#col-clip)")
            .attr("transform", "translate(150 0)");

        svgGroups
            .rowAxis
            .attr("clip-path", "url(#row-clip)")
            .attr("transform", "translate(0 50)");

        svgGroups
            .rowContent
            .append('rect')
            .attr('width', 150)
            .attr('height', 750)
            .attr('fill', 'red');

        svgGroups
            .columnContent
            .append('rect')
            .attr('width', 1450)
            .attr('height', 50)
            .attr('fill', 'green');

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

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
import template from "./roadmap-diagram.html";
import {select} from "d3-selection";
import {createGroupElements} from "../../../common/d3-utils";
import {setupZoom} from "./roadmap-diagram-utils";
import {draw} from "./roadmap-diagram-render";


const bindings = {
    parentEntityRef: "<",
    scope: "@"
};


const initialState = {};


function setupGroupElements($element) {
    const svg = select($element.find("svg")[0]);
    const definitions = [
        {
            name: "holder",
            children: [
                { name: "grid", children: [ { name: "gridContent" }] },
                { name: "columns", children: [ { name: "columnHeaders" }] },
                { name: "rowGroups", children: [ { name: "rowGroupHeaders" }]  }
            ]
        }
    ];
    return Object.assign({}, { svg }, createGroupElements(svg, definitions));
}


function controller($element) {
    const vm = initialiseData(this, initialState);

    let svgGroups = null;

    function redraw() {
        if (svgGroups) {
            draw(
                svgGroups,
                gridDefn);
        }
    }

    vm.$onInit = () => {
        svgGroups = setupGroupElements($element);
        setupZoom(svgGroups);
        redraw();
    };

    vm.$onChanges = (changes) => {
        console.log("roadmap-diagram changes - parentEntityRef: ", vm.parentEntityRef);
        redraw();
    };


    // --- play

    const columns = [];
    const rowGroups = [];
    const gridDefn = { columns, rowGroups };

    let ctr = 10;
    let rg = null;

    function addRow() {
        ctr++;
        const name = (ctr % 2 ? "row definition " : "row def") + ctr;
        const newElem  = {
            id: ctr,
            datum: { name }
        };
        rg.rows.push(newElem);
    }


    function addCol() {
        ctr++;
        const name = (ctr % 2 ? "column definition " : "col def") + ctr;
        const newElem  = {
            id: ctr,
            datum: { name }
        };
        columns.push(newElem);
    }


    function addRowGroup() {
        ctr++;
        const name = (ctr % 2 ? "row group definition " : "row grp") + ctr;
        rg = {
            id: ctr,
            datum: { name },
            rows: []
        };
        rowGroups.push(rg);
    }


    vm.onAddRow = () => {
        addRow();
        redraw();
    };


    vm.onAddCol = () => {
        addCol();
        redraw();
    };


    vm.onAddRowGroup = () => {
        addRowGroup();
        redraw();
    };


    // setup sample data
    addCol();addCol();addCol();
    addRowGroup();addRow();addRow();
    addRowGroup();
    addRowGroup();addRow();
    addRowGroup();addRow();
    addRowGroup();addRow();addRow();addRow();addRow();

}


controller.$inject = ["$element"];


const component = {
    template,
    bindings,
    controller
};


export default {
    component,
    id: "waltzRoadmapDiagram"
};

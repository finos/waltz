/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016  Khartec Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import {initialiseData} from "../../../common";
import {mkEntityLinkGridCell, mkLinkGridCell} from "../../../common/link-utils";


const template = require('./physical-flow-lineage-panel.html');


const bindings = {
    lineage: '<',
    onInitialise: '<'
};


const initialState = {
    lineage: [],
    onInitialise: (e) => {}
};


function controller() {

    const vm = initialiseData(this, initialState);

    vm.columnDefs = [
        Object.assign(mkLinkGridCell('Specification', 'specification.name', 'flow.id', 'main.physical-flow.view'), { width: "20%"} ),
        Object.assign(mkEntityLinkGridCell('From', 'sourceEntity', 'left'), { width: "15%" }),
        Object.assign(mkEntityLinkGridCell('To', 'targetEntity', 'left'), { width: "15%" }),
        { field: 'specification.format', displayName: 'Format', width: "12%" },
        { field: 'flow.transport', displayName: 'Transport', width: "12%" },
        { field: 'flow.description', displayName: 'Description', width: "26%" }
    ];

    vm.onGridInitialise = (e) => {
        vm.exportFn = e.exportFn;
    };

    vm.exportGrid = () => {
        vm.exportFn('lineage.csv');
    };

    // callback
    vm.onInitialise({
        exportFn: vm.exportGrid
    });

}


controller.$inject = [];


const component = {
    bindings,
    template,
    controller
};


export default component;
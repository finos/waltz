/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017  Waltz open source project
 * See README.md for more information
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

import { initialiseData } from "../../../common";
import template from "./change-set-panel.html";


const bindings = {
    changeSets: "<"
};


const initialState = {
    columnDefs: [],
    csvName: "change-sets.csv"
};



function mkColumnDefs() {

    return [
        {
            field: "name",
            name: "Name",
            width: "15%",
            cellTemplate: `
                <div class="ui-grid-cell-contents">
                    <waltz-entity-link entity-ref="row.entity" 
                                       icon-placement="left">
                    </waltz-entity-link>
                </div>`
        },
        {
            field: "externalId",
            name: "External Id",
            width: "10%"
        },
        {
            field: "description",
            name: "Description",
            width: "65%",
            cellTemplate: "<div class=\"ui-grid-cell-contents\"><span title=\"{{COL_FIELD}}\" ng-bind=\"COL_FIELD\"></span></div>"
        },
        {
            field: "plannedDate",
            name: "Planned",
            width: "10%",
            cellTemplate: "<div class=\"ui-grid-cell-contents\"><waltz-from-now timestamp=\"COL_FIELD\"></waltz-from-now></div>"
        }
    ];
}


function controller() {
    const vm = initialiseData(this, initialState);

    vm.$onChanges = (changes) => {
        if(changes.changeSets) {
            vm.columnDefs = mkColumnDefs();
        }
    };


    vm.onGridInitialise = (opts) => {
        vm.gridExportFn = opts.exportFn;
    };


    vm.exportChangeSets = () => {
        vm.gridExportFn(vm.csvName);
    };
}


controller.$inject = [];


const component = {
    template,
    bindings,
    controller
};


export default {
    component,
    id: "waltzChangeSetPanel"
};


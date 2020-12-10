/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017, 2018, 2019 Waltz open source project
 * See README.md for more information
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific
 *
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


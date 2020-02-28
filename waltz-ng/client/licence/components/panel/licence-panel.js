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
import { mkLinkGridCell } from "../../../common/grid-utils";

import template from "./licence-panel.html";

const bindings = {
    licences: "<"
};


const initialState = {
    columnDefs: [],
    csvName: "licences.csv"
};


function mkColumnDefs() {
    return [
        mkLinkGridCell("Name", "name", "id", "main.licence.view"),
        { field: "externalId", displayName: "External Id" },
        {
            field: "assessments",
            displayName: "Assessments",
            width: "7%",
            cellTemplate: `
                <div style="text-align: center">
                    <waltz-assessment-rating-traffic-lights assessments="COL_FIELD">
                    </waltz-assessment-rating-traffic-lights>
                </div>`
        }
    ];
}


function controller() {
    const vm = initialiseData(this, initialState);

    vm.$onChanges = (changes) => {
        if(changes.licences) {
            vm.columnDefs = mkColumnDefs();
        }
    };


    vm.onGridInitialise = (opts) => {
        vm.gridExportFn = opts.exportFn;
    };


    vm.exportLicences = () => {
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
    id: "waltzLicencePanel"
};


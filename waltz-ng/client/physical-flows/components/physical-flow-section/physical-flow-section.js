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

import template from "./physical-flow-section.html";
import {initialiseData} from "../../../common";
import {columnDef} from "../../../physical-flow/physical-flow-table-utilities";


const bindings = {
    parentEntityRef: "<",
};

const initialState = {
    columnDefs: []
};

function determineColumnDefs(entityKind) {
    switch (entityKind) {
        case "PHYSICAL_SPECIFICATION":
            return [
                columnDef.name,
                columnDef.extId,
                columnDef.source,
                columnDef.target,
                columnDef.frequency,
                columnDef.transport,
                columnDef.description];
        case "TAG":
            return [
                columnDef.name,
                columnDef.extId,
                columnDef.source,
                columnDef.target,
                columnDef.frequency,
                columnDef.description];
        default:
            return initialState.columnDefs;

    }
}

function controller(){
    const vm = initialiseData(this, initialState);

    vm.$onInit = () => {
        vm.columnDefs = determineColumnDefs(vm.parentEntityRef.kind);
    }
}


const component = {
    template,
    controller,
    bindings,
};


export default {
    component,
    id: "waltzPhysicalFlowSection"
};

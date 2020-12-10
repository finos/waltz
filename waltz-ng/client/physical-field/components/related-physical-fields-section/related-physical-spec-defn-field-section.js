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

import _ from "lodash";
import {CORE_API} from "../../../common/services/core-api-utils";
import {initialiseData} from "../../../common";
import {mkSelectionOptions} from "../../../common/selector-utils";
import {mkLinkGridCell} from "../../../common/grid-utils";

import template from "./related-physical-spec-defn-field-section.html";


const bindings = {
    parentEntityRef: "<",
};


const initialState = {
    selector: null,
    columnDefs: [],
    data: [],
    physicalFields: [],
    physicalSpecs: [],
    physicalSpecDefns: []
};


function mkColumnDefs() {
    return [
        {
            field: "field.name",
            name: "Name",
        },
        {
            field: "field.type",
            name: "Type",
        },
        Object.assign(
            {},
            mkLinkGridCell("Specification", "specification.name", "specification.id", "main.physical-specification.view"),
            { width: "20%"}
        ),
        {
            field: "specification.format",
            name: "Format",
        },
        {
            field: "specDefn.version",
            name: "Version",
        },
        {
            field: "specDefn.status",
            name: "Status",
        },
        {
            field: "field.description",
            name: "Description",
            width: "25%"
        }
    ];
}


function controller($q, serviceBroker) {
    const vm = initialiseData(this, initialState);

    vm.$onInit = () => {
        vm.columnDefs = mkColumnDefs();
    };

    vm.$onChanges = (changes) => {
        if(changes.parentEntityRef && vm.parentEntityRef) {
            vm.selector = mkSelectionOptions(vm.parentEntityRef);
            const fieldsPromise = serviceBroker
                .loadViewData(CORE_API.PhysicalSpecDefinitionFieldStore.findBySelector, [vm.selector])
                .then(r => r.data);

            const spedDefnsPromise = serviceBroker
                .loadViewData(CORE_API.PhysicalSpecDefinitionStore.findBySelector, [vm.selector])
                .then(r => r.data);

            const specsPromise = serviceBroker
                .loadViewData(CORE_API.PhysicalSpecificationStore.findBySelector, [vm.selector])
                .then(r => r.data);

            $q.all([fieldsPromise, spedDefnsPromise, specsPromise])
                .then(([fields, defns, specs]) => {
                    vm.physicalFields = fields;
                    vm.physicalSpecDefns = defns;
                    vm.physicalSpecs = specs;

                    const defnsById = _.keyBy(defns, "id");
                    const specsById = _.keyBy(specs, "id");

                    vm.data = _.map(fields, f => {
                        const specDefn = defnsById[f.specDefinitionId];
                        const specification = specsById[specDefn.specificationId];
                        return Object.assign({}, {field: f}, {specDefn}, {specification});
                    });
                });
        }
    };

}


controller.$inject = [
    "$q",
    "ServiceBroker"
];


const component = {
    template,
    bindings,
    controller
};


export default {
    component,
    id: "waltzRelatedPhysicalSpecDefnFieldSection"
};

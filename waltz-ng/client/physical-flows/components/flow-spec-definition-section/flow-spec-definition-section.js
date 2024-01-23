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

import template from "./flow-spec-definition-section.html";
import {CORE_API} from "../../../common/services/core-api-utils";
import {toEntityRefWithKind} from "../../../common/entity-utils";
import {initialiseData} from "../../../common";
import _ from "lodash";
import toasts from "../../../svelte-stores/toast-store";


const bindings = {
    parentEntityRef: "<"
};

const initialState = {
    specification: null,
    selectedSpecDefinition: {},
    selectableSpecDefinitions: [],
};


function getSelectedSpecDefinition(specDefinitions = [], selectedSpecDefId = null) {
    if (selectedSpecDefId) {
        const defsById = _.keyBy(specDefinitions, "id");
        return defsById[selectedSpecDefId];
    } else {
        // find the active definition
        return _.find(specDefinitions, d => d.status === "ACTIVE");
    }
}


function getSelectableSpecDefinitions(specDefinitions = [], selectedSpecDef) {
    if (selectedSpecDef) {
        return _.filter(specDefinitions, sd => sd.id !== selectedSpecDef.id);
    }

    return specDefinitions;
}


function controller($q, serviceBroker) {

    const vm = initialiseData(this, initialState);


    // spec definitions
    const loadSpecDefinitions = (force = false) => serviceBroker
        .loadViewData(
            CORE_API.PhysicalSpecDefinitionStore.findForSpecificationId,
            [vm.physicalFlow.specificationId])
        .then(r => {
            vm.selectedSpecDefinition.def = getSelectedSpecDefinition(r.data, vm.physicalFlow.specificationDefinitionId);
            vm.selectableSpecDefinitions = getSelectableSpecDefinitions(r.data, vm.selectedSpecDefinition.def);
        })
        .then(() => {
            if (vm.selectedSpecDefinition.def) {
                const specDefFieldPromise = serviceBroker
                    .loadViewData(
                        CORE_API.PhysicalSpecDefinitionFieldStore.findForSpecDefinitionId,
                        [ vm.selectedSpecDefinition.def.id ],
                        { force })
                    .then(r => r.data);

                const specDefSampleFilePromise = serviceBroker
                    .loadViewData(
                        CORE_API.PhysicalSpecDefinitionSampleFileStore.findForSpecDefinitionId,
                        [vm.selectedSpecDefinition.def.id],
                        { force })
                    .then(r => r.data);

                const selectionOptions = {
                    scope: "EXACT",
                    entityReference: { kind: "PHYSICAL_SPECIFICATION", id:vm.selectedSpecDefinition.def.specificationId }
                };

                const logicalElementsPromise = serviceBroker
                    .loadViewData(
                        CORE_API.LogicalDataElementStore.findBySelector,
                        [ selectionOptions ],
                        { force })
                    .then(r => r.data);

                $q.all([specDefFieldPromise, specDefSampleFilePromise, logicalElementsPromise])
                    .then(([fields, file, logicalElements]) => {
                        vm.selectedSpecDefinition.fields = fields;
                        vm.selectedSpecDefinition.sampleFile = file;
                        vm.logicalDataElements = logicalElements;
                    });
            }
        });


    vm.$onInit = () => {

        const physicalFlowPromise = serviceBroker
            .loadViewData(
                CORE_API.PhysicalFlowStore.getById,
                [ vm.parentEntityRef.id ])
            .then(r => vm.physicalFlow = r.data);


        physicalFlowPromise
            .then(() => serviceBroker
                .loadViewData(
                    CORE_API.LogicalFlowStore.getById,
                    [vm.physicalFlow.logicalFlowId]))
            .then(r => vm.logicalFlow = r.data);

        const specPromise = physicalFlowPromise
            .then(physicalFlow => serviceBroker
                .loadViewData(
                    CORE_API.PhysicalSpecificationStore.getById,
                    [physicalFlow.specificationId]))
            .then(r => {
                vm.specification = r.data;
                vm.specificationReference = toEntityRefWithKind(r.data, "PHYSICAL_SPECIFICATION");
            });

        specPromise
            .then(() => loadSpecDefinitions())
    }

    // INTERACT
    vm.updateSpecDefinitionId = (newSpecDef) => {
        if (confirm("Are you sure you want to change the specification definition version used by this flow ?")) {
            serviceBroker
                .execute(
                    CORE_API.PhysicalFlowStore.updateSpecDefinitionId,
                    [vm.parentEntityRef.id, { newSpecDefinitionId: newSpecDef.id }])
                .then(() => {
                    vm.physicalFlow.specificationDefinitionId = newSpecDef.id;
                    loadSpecDefinitions();
                    toasts.success("Specification definition version updated successfully");
                });
        }
    };

    vm.updateFieldDescription = (change, field) => {
        const cmd = { newDescription: change.newVal };
        serviceBroker
            .execute(CORE_API.PhysicalSpecDefinitionFieldStore.updateDescription, [field.id, cmd])
            .then(result => {
                if (result) {
                    toasts.success("Updated description for field");
                    loadSpecDefinitions(true);
                } else {
                    toasts.error("Could not update field description");
                }
            });
    };


    vm.updateLogicalDataElement = (change, field) => {
        const cmd = { newLogicalDataElement: change.newVal };
        serviceBroker
            .execute(CORE_API.PhysicalSpecDefinitionFieldStore.updateLogicalElement, [field.id, cmd])
            .then(result => {
                if (result) {
                    toasts.success("Updated logical data element for field");
                    loadSpecDefinitions(true);
                } else {
                    toasts.error("Could not update logical data element");
                }
            });
    };

}

/*
spec-definition="$ctrl.selectedSpecDefinition"
logical-data-elements="$ctrl.logicalDataElements"
selectable-definitions="$ctrl.selectableSpecDefinitions"
Y on-definition-select="$ctrl.updateSpecDefinitionId"
Y on-update-field-description="$ctrl.updateFieldDescription"
on-update-logical-data-element="$ctrl.updateLogicalDataElement">

 */

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
    id: "waltzFlowSpecDefinitionSection"
}
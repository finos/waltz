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
import {initialiseData} from "../common";
import {kindToViewState} from "../common/link-utils";
import {logicalFlow} from "./svelte/physical-flow-editor-store";


import template from "./physical-flow-registration.html";
import {CORE_API} from "../common/services/core-api-utils";
import {loadEntity} from "../common/entity-utils";
import {removeEnrichments} from "./physical-flow-utils";
import {columnDef, withWidth} from "../physical-flow/physical-flow-table-utilities";
import PhysicalFlowRegistrationView from "./svelte/PhysicalFlowRegistrationView.svelte";
import toasts from "../svelte-stores/toast-store";


const initialState = {
    cancelLink: "#/",
    existingSpecifications: [],
    flowAttributes: null,
    sourceEntity: null,
    specification: null,
    targetLogicalFlow: null,
    existingTargets: [],
    existingFlowsByTarget: {},
    similarFlows: [],
    validation: {
        messages: [],
        canSave: false
    },
    visibility: {
        editor: "",
        loading: false,
        similarFlows: false
    },
    PhysicalFlowRegistrationView
};


function validate(specification, flowAttributes, targetLogicalFlow) {

    const messages = [];
    messages.push(specification ? null : "Missing specification. ");
    messages.push(flowAttributes ? null : "Missing attributes. ");
    messages.push(targetLogicalFlow ? null : "Missing target. ");

    return {
        canSave: specification && flowAttributes && targetLogicalFlow,
        messages: _.reject(messages, m => m == null)
    };
}


function toAttributes(physFlow) {
    return {
        transport: physFlow.transport,
        frequency: physFlow.frequency,
        criticality: physFlow.criticality,
        basisOffset: physFlow.basisOffset
    };
}


function controller(
    $scope,
    $state,
    $stateParams,
    serviceBroker,
    preventNavigationService) {

    const vm = initialiseData(this, initialState);
    preventNavigationService.setupWarningDialog($scope, () => isDirty());

    vm.similarFlowDefs = [
        withWidth(columnDef.name, "10%"),
        withWidth(columnDef.format, "10%"),
        withWidth(columnDef.transport, "14%"),
        withWidth(columnDef.frequency, "10%"),
        withWidth(columnDef.basisOffset, "10%"),
        columnDef.description
    ];


    const sourceEntityRef = {
        id: $stateParams.id,
        kind: $stateParams.kind
    };

    const targetFlowId = $stateParams.targetLogicalFlowId;

    if (targetFlowId) {
        serviceBroker
            .loadViewData(CORE_API.LogicalFlowStore.getById, [targetFlowId])
            .then(r => {
                logicalFlow.set(r.data);
                vm.targetChanged(r.data);
            });
    }

    const viewState = kindToViewState(sourceEntityRef.kind);
    vm.cancelLink = $state.href(viewState, { id: sourceEntityRef.id });

    const doValidation = () => {
        vm.visibility.similarFlows = false;
        // vm.similarFlows = findSimilarFlows(vm.specification, vm.flowAttributes, vm.targetEntity, vm.existingFlowsByTarget);
        return validate(vm.specification, vm.flowAttributes, vm.targetLogicalFlow);
    };

    const isDirty = () => {
        return vm.specification || vm.flowAttributes || vm.targetLogicalFlow;
    };

    vm.focusSpecification = () => {
        vm.visibility.editor = "SPECIFICATION";
    };

    vm.focusFlowAttributes = () => {
        vm.visibility.editor = "FLOW-ATTRIBUTES";
    };

    vm.focusTarget = () => {
        vm.visibility.editor = "TARGET-LOGICAL-FLOW";
    };

    vm.focusClone = () => {
        vm.visibility.editor = "CLONE";
        vm.visibility.loading = true;
    };

    vm.attributesChanged = (attributes) => {
        vm.flowAttributes = attributes;
        vm.editorDismiss();
    };

    vm.targetChanged = (logicalFlow) => {
        vm.targetLogicalFlow = logicalFlow;
        vm.editorDismiss();
    };


    vm.specificationChanged = (spec) => {
        vm.specification = spec;
        vm.editorDismiss();
    };

    vm.editorDismiss = () => {
        vm.visibility.editor = "";
        vm.validation = doValidation();
    };

    vm.onClone = flow => {
        vm.specification = removeEnrichments(flow.specification);
        vm.flowAttributes = toAttributes(flow.physical);
        vm.targetLogicalFlow = flow.logical;
        vm.editorDismiss();
        toasts.info("Flow has been cloned, please make some changes before saving.")
    };

    vm.doSave = () => {
        const validationResult = doValidation();
        if (validationResult.canSave) {
            const cmd = {
                specification: vm.specification,
                flowAttributes: vm.flowAttributes,
                logicalFlowId: vm.targetLogicalFlow.id
            };
            serviceBroker
                .execute(CORE_API.PhysicalFlowStore.create, [cmd])
                .then(r => {
                    const resp = r.data;
                    if(resp.outcome === "SUCCESS") {
                        const successMessage = vm.specification.isRemoved
                            ? "Created new flow and activated the selected specification."
                            : "Created new flow";
                        toasts.info(successMessage);
                    } else if(resp.outcome === "FAILURE") {
                        toasts.warning(resp.message + ", redirected to existing.")
                    }
                    if(resp.entityReference) {
                        //clear variables
                        vm.specification = null;
                        vm.flowAttributes = null;
                        vm.targetLogicalFlow = null;
                        $state.go("main.physical-flow.view", {id: resp.entityReference.id});
                    }
                });
        } else {
            const messages =  _.join(validationResult.messages, "<br> - ");
            toasts.warning("Cannot save: <br> - " + messages);
        }
    };


    loadEntity(serviceBroker, sourceEntityRef)
        .then(ent => vm.sourceEntity = ent);

    serviceBroker
        .loadViewData(CORE_API.PhysicalSpecificationStore.findByEntityReference, [sourceEntityRef])
        .then(r =>  vm.existingSpecifications = r.data);

    serviceBroker
        .loadViewData(CORE_API.LogicalFlowStore.findByEntityReference, [ sourceEntityRef ])
        .then(r =>
            vm.outboundLogicalFlows = _
                .chain(r.data)
                .filter(f => f.source.kind === sourceEntityRef.kind && f.source.id === sourceEntityRef.id)
                .orderBy("target.name")
                .value());
}


controller.$inject = [
    "$scope",
    "$state",
    "$stateParams",
    "ServiceBroker",
    "PreventNavigationService"
];


export default {
    template,
    controller,
    controllerAs: "ctrl"
};

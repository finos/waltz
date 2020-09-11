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
import {allEntityLifecycleStatuses, initialiseData} from "../common";
import {CORE_API} from "../common/services/core-api-utils";
import {downloadTextFile} from "../common/file-utils";

import template from "./flow-diagram-view.html";
import {entity} from "../common/services/enums/entity";

const initialState = {
    visibility: {},
};


function prepareDataForExport(flows = []) {
    const columnNames = [[
        "Source",
        "Target",
        "Transport",
        "Frequency",
        "Specification",
        "Data Types"
    ]];

    const exportData = _.flatMap(flows, f => {
        if (f.physicalFlows.length > 0) {
            return _.map(f.physicalFlows, pf => [
                f.logicalFlow.source.name,
                f.logicalFlow.target.name,
                pf.transportName,
                pf.frequencyName,
                pf.specificationName,
                _.join(_.map(pf.specificationDataTypes, "dataTypeName"), ";")
            ]);
        } else {
            return [[
                f.logicalFlow.source.name,
                f.logicalFlow.target.name,
                "",
                "",
                "",
                ""
            ]];
        }
    });

    return columnNames.concat(exportData);
}


function controller(
    $q,
    $stateParams,
    $timeout,
    displayNameService,
    dynamicSectionManager,
    flowDiagramStateService,
    serviceBroker)
{
    const vm = initialiseData(this, initialState);

    vm.$onInit = () => {
        const id = $stateParams.id;
        const entityReference = { id, kind: "FLOW_DIAGRAM" };
        dynamicSectionManager.initialise("FLOW_DIAGRAM");
        vm.parentEntityRef = entityReference;

        const selector = {
            entityReference: vm.parentEntityRef,
            scope: "EXACT"
        };

        const selectorWithAllLifeCycleStatuses = {
            entityReference: vm.parentEntityRef,
            scope: "EXACT",
            entityLifecycleStatuses: allEntityLifecycleStatuses
        };

        flowDiagramStateService
            .load(id)
            .then(loadVisibility);

        serviceBroker
            .loadViewData(CORE_API.FlowDiagramStore.getById, [ id ])
            .then(r => vm.diagram = r.data);

        serviceBroker
            .loadViewData(CORE_API.FlowDiagramEntityStore.findByDiagramId, [ id ])
            .then(r => {
                vm.nodes = _
                    .chain(r.data)
                    .map("entityReference")
                    .filter(x => x.kind === "APPLICATION" || x.kind === "ACTOR")
                    .sortBy("name")
                    .value();
            });

        const physicalFlowPromise = serviceBroker
            .loadViewData(CORE_API.PhysicalFlowStore.findBySelector, [ selectorWithAllLifeCycleStatuses ])
            .then(r => r.data);

        const physicalSpecPromise =  serviceBroker
            .loadViewData(CORE_API.PhysicalSpecificationStore.findBySelector, [ selectorWithAllLifeCycleStatuses ])
            .then(r => r.data);

        const physicalSpecDataTypesPromise = serviceBroker
            .loadViewData(CORE_API.DataTypeDecoratorStore.findBySelector, [ selector, entity.PHYSICAL_SPECIFICATION.key ])
            .then(result => result.data);

        const logicalFlowPromise = serviceBroker
            .loadViewData(CORE_API.LogicalFlowStore.findBySelector, [ selector ])
            .then(r => r.data);

        $q.all([logicalFlowPromise, physicalFlowPromise, physicalSpecPromise, physicalSpecDataTypesPromise])
            .then(([logicalFlows, physicalFlows, physicalSpecs, physicalSpecDataTypes]) => {
                const physicalSpecsById = _.keyBy(physicalSpecs, "id");
                const physicalSpecDataTypesBySpecId =
                    _.chain(physicalSpecDataTypes)
                        .map(psdt => Object.assign(
                            {},
                            psdt,
                            { dataTypeName: displayNameService.lookup("dataType", psdt.dataTypeId) }))
                        .groupBy("entityReference.id")
                        .value();
                const enhancedPhysicalFlows = _.map(physicalFlows, pf => Object.assign(
                    {},
                    pf,
                    {
                        transportName: displayNameService.lookup("TransportKind", pf.transport),
                        frequencyName: displayNameService.lookup("frequencyKind", pf.frequency),
                        specificationName: physicalSpecsById[pf.specificationId]
                            ? physicalSpecsById[pf.specificationId].name
                            : "-",
                        specificationDataTypes: physicalSpecDataTypesBySpecId[pf.specificationId] || []
                    }));
                const enhancedPhysicalFlowsByLogicalId = _.groupBy(enhancedPhysicalFlows, "logicalFlowId");

                vm.flows = _.map(logicalFlows, f => {
                    return {
                        logicalFlow: f,
                        physicalFlows: enhancedPhysicalFlowsByLogicalId[f.id] || [],
                    }
                });
            });

    };

    const loadVisibility = () =>
        vm.visibility.layers = flowDiagramStateService.getState().visibility.layers;

    vm.clickHandlers =  {
        node: (d) => $timeout(
            () => vm.highlightIds = [d.data.id],
            0),
        flowBucket: (d) => $timeout(
            () => vm.highlightIds = [d.data.id],
            0)
    };

    vm.toggleLayer = (layer) => {
        const currentlyVisible = flowDiagramStateService.getState().visibility.layers[layer];
        const cmd = {
            command: currentlyVisible ? "HIDE_LAYER" : "SHOW_LAYER",
            payload: layer
        };
        flowDiagramStateService.processCommands([cmd]);
        loadVisibility();
    };

    vm.exportDiagramTable = () => {
        const dataRows = prepareDataForExport(vm.flows);
        downloadTextFile(dataRows, ",", vm.diagram.name + "_flows.csv");
    };


    // -- INTERACT --
    vm.addSection = (section) => vm.sections = dynamicSectionManager.openSection(section, "FLOW_DIAGRAM");
    vm.removeSection = (section) => vm.sections = dynamicSectionManager.removeSection(section, "FLOW_DIAGRAM");

}

controller.$inject = [
    "$q",
    "$stateParams",
    "$timeout",
    "DisplayNameService",
    "DynamicSectionManager",
    "FlowDiagramStateService",
    "ServiceBroker"
];

const view = {
    template,
    controller,
    controllerAs: "ctrl"
};

export default view;
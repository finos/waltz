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
import {mkEntityLinkGridCell} from "../common/grid-utils";
import {withWidth} from "../physical-flow/physical-flow-table-utilities";

const initialState = {
    visibility: {},
};

const columnDefs = [
    withWidth(mkEntityLinkGridCell("Source", "logicalFlow.source"), "20%"),
    withWidth(mkEntityLinkGridCell("Target", "logicalFlow.target"), "20%"),
    { field: "physicalFlow.transportName",
      width: "10%",
      name: "Transport",
      cellTemplate:`
                <div class="ui-grid-cell-contents"
                    <span ng-bind="COL_FIELD || '-'">
                    </span>
                </div>`
    },
    { field: "physicalFlow.frequencyName",
      name: "Frequency",
      width: "10%",
      cellTemplate:`
                <div class="ui-grid-cell-contents"
                    <span ng-bind="COL_FIELD || '-'">
                    </span>
                </div>`
    },
    withWidth(mkEntityLinkGridCell("Specification", "specification"), "20%"),
    { field: "dataTypes",
      name: "Data Types",
      width: "20%",
      cellTemplate:`
                <div class="ui-grid-cell-contents"
                    <span ng-bind="COL_FIELD || '-'">
                    </span>
                </div>`
    },
];


const addToHistory = (historyStore, diagram) => {
    if (! diagram) { return; }
    historyStore.put(
        diagram.name,
        "FLOW_DIAGRAM",
        "main.flow-diagram.view",
        { id: diagram.id });
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

    const exportData = _.map(flows, f => [
        f.logicalFlow.source.name,
        f.logicalFlow.target.name,
        _.get(f.physicalFlow, "transportName", "-"),
        _.get(f.physicalFlow, "frequencyName", "-"),
        _.get(f.specification, "name", "-"),
        f.dataTypes || "-"
    ]);

    return columnNames.concat(exportData);
}


function controller(
    $q,
    $stateParams,
    $timeout,
    displayNameService,
    flowDiagramStateService,
    historyStore,
    serviceBroker)
{
    const vm = initialiseData(this, initialState);

    vm.$onInit = () => {
        const id = $stateParams.id;
        const entityReference = { id, kind: "FLOW_DIAGRAM" };
        vm.parentEntityRef = entityReference;

        vm.columnDefs = columnDefs;

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

        serviceBroker
            .loadViewData(CORE_API.FlowDiagramStore.getById, [ id ])
            .then(r => {
                vm.diagram = r.data;
                addToHistory(historyStore, vm.diagram);
            });

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


                vm.flowData = _.flatMap(logicalFlows, f => {

                    const physicals = enhancedPhysicalFlowsByLogicalId[f.id] || [];

                    if (_.isEmpty(physicals)){
                        return Object.assign({}, {logicalFlow: f, physicalFlow: null, dataTypes: null});
                    } else {
                        return _.map(
                            physicals,
                            pf => {
                                const dataTypes = _.join(_.map(pf.specificationDataTypes, "dataTypeName"), "; ")

                                return Object.assign({}, {
                                    logicalFlow: f,
                                    physicalFlow: pf,
                                    dataTypes,
                                    specification: {id: pf.specificationId, kind: "PHYSICAL_SPECIFICATION", name: pf.specificationName}
                                })
                            });
                    }
                })
            });

    };

    vm.clickHandlers =  {
        node: (d) => $timeout(
            () => vm.highlightIds = [d.data.id],
            0),
        flowBucket: (d) => $timeout(
            () => vm.highlightIds = [d.data.id],
            0)
    };

    vm.exportDiagramTable = () => {
        const dataRows = prepareDataForExport(vm.flowData);
        downloadTextFile(dataRows, ",", vm.diagram.name + "_flows.csv");
    };

}

controller.$inject = [
    "$q",
    "$stateParams",
    "$timeout",
    "DisplayNameService",
    "FlowDiagramStateService",
    "HistoryStore",
    "ServiceBroker"
];

const view = {
    template,
    controller,
    controllerAs: "ctrl"
};

export default view;
/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017 Waltz open source project
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
import _ from "lodash";
import {initialiseData} from "../common";
import {CORE_API} from "../common/services/core-api-utils";
import {downloadTextFile} from "../common/file-utils";
import {dynamicSections} from "../dynamic-section/dynamic-section-definitions";

import template from './flow-diagram-view.html';

const initialState = {
    visibility: {},
    appsSection: dynamicSections.appsSection,
    bookmarksSection: dynamicSections.bookmarksSection,
    changeLogSection: dynamicSections.changeLogSection,
    entityStatisticSummarySection: dynamicSections.entityStatisticSummarySection,
    measurableRatingsBrowserSection: dynamicSections.measurableRatingsBrowserSection,
    technologySummarySection: dynamicSections.technologySummarySection
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
                _.join(_.map(pf.specificationDataTypes, 'dataTypeName'), ";")
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
    flowDiagramStateService,
    flowDiagramStore,
    flowDiagramEntityStore,
    logicalFlowStore,
    physicalFlowStore,
    physicalSpecificationStore,
    serviceBroker)
{
    const vm = initialiseData(this, initialState);
    const diagramId = $stateParams.id;

    vm.entityReference = {
        id: diagramId,
        kind: 'FLOW_DIAGRAM'
    };

    const loadVisibility = () =>
        vm.visibility.layers = flowDiagramStateService.getState().visibility.layers;

    flowDiagramStateService
        .load(diagramId)
        .then(loadVisibility);

    flowDiagramStore
        .getById(diagramId)
        .then(d => vm.diagram = d);

    flowDiagramEntityStore
        .findByDiagramId(diagramId)
        .then(xs => {
            vm.nodes = _
                .chain(xs)
                .map('entityReference')
                .filter(x => x.kind === 'APPLICATION' || x.kind === 'ACTOR')
                .sortBy('name')
                .value();
        });

    const selector = {
        entityReference: { id: diagramId, kind: 'FLOW_DIAGRAM' },
        scope: 'EXACT'
    };

    const physicalFlowPromise = physicalFlowStore
        .findBySelector(selector);

    const physicalSpecPromise = physicalSpecificationStore
        .findBySelector(selector)
        .then(xs => {
            return xs;
        });

    const physicalSpecDataTypesPromise = serviceBroker
        .loadViewData(CORE_API.PhysicalSpecDataTypeStore.findBySpecificationSelector, [selector])
        .then(result => result.data);

    const logicalFlowPromise = logicalFlowStore
        .findBySelector(selector);

    $q.all([logicalFlowPromise, physicalFlowPromise, physicalSpecPromise, physicalSpecDataTypesPromise])
        .then(([logicalFlows, physicalFlows, physicalSpecs, physicalSpecDataTypes]) => {
            const physicalSpecsById = _.keyBy(physicalSpecs, 'id');
            const physicalSpecDataTypesBySpecId =
                _.chain(physicalSpecDataTypes)
                    .map(psdt => Object.assign(
                        {},
                        psdt,
                        { dataTypeName: displayNameService.lookup('dataType', psdt.dataTypeId) }))
                    .groupBy('specificationId')
                    .value();
            const enhancedPhysicalFlows = _.map(physicalFlows, pf => Object.assign(
                {},
                pf,
                {
                    transportName: displayNameService.lookup('transportKind', pf.transport),
                    frequencyName: displayNameService.lookup('frequencyKind', pf.frequency),
                    specificationName: physicalSpecsById[pf.specificationId]
                                        ? physicalSpecsById[pf.specificationId].name
                                        : "-",
                    specificationDataTypes: physicalSpecDataTypesBySpecId[pf.specificationId] || []
                }));
            const enhancedPhysicalFlowsByLogicalId = _.groupBy(enhancedPhysicalFlows, 'logicalFlowId');

            vm.flows = _.map(logicalFlows, f => {
                return {
                    logicalFlow: f,
                    physicalFlows: enhancedPhysicalFlowsByLogicalId[f.id] || [],
                }
            });
        });


    vm.clickHandlers =  {
        node: d => $timeout(
            () => vm.highlightIds = [d.data.id],
            0),
        flowBucket: d => $timeout(
            () => vm.highlightIds = [d.data.id],
            0)
    };

    vm.toggleLayer = (layer) => {
        const currentlyVisible = flowDiagramStateService.getState().visibility.layers[layer];
        const cmd = {
            command: currentlyVisible ? 'HIDE_LAYER' : 'SHOW_LAYER',
            payload: layer
        };
        flowDiagramStateService.processCommands([cmd]);
        loadVisibility();
    };

    vm.exportDiagramTable = () => {
        const dataRows = prepareDataForExport(vm.flows);
        downloadTextFile(dataRows, ",", vm.diagram.name + "_flows.csv");
    };

}

controller.$inject = [
    '$q',
    '$stateParams',
    '$timeout',
    'DisplayNameService',
    'FlowDiagramStateService',
    'FlowDiagramStore',
    'FlowDiagramEntityStore',
    'LogicalFlowStore',
    'PhysicalFlowStore',
    'PhysicalSpecificationStore',
    'ServiceBroker'
];

const view = {
    template,
    controller,
    controllerAs: 'ctrl'
};

export default view;
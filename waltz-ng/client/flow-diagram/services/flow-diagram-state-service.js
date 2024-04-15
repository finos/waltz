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
import {ifPresent} from "../../common/function-utils";
import {toGraphFlow, toGraphId, toGraphNode} from "../flow-diagram-utils";
import {toEntityRef} from "../../common/entity-utils";
import {mkSelectionOptions} from "../../common/selector-utils";
import newModel from "../components/diagram-svelte/store/model"
import {diagramTransform, positions} from "../components/diagram-svelte/store/layout";
import dirty from "../components/diagram-svelte/store/dirty";
import overlay from "../components/diagram-svelte/store/overlay";
import {diagram} from "../components/diagram-svelte/store/diagram";

function restoreDiagram(
    flowDiagram,
    annotations = [],
    entityNodes = [],
    logicalFlows = [],
    physicalFlows = []) {

    diagram.set(flowDiagram);

    const layoutData = JSON.parse(flowDiagram.layoutData);
    const logicalFlowsById = _.keyBy(logicalFlows, "id");

    // nodes
    _.chain(entityNodes)
        .filter(ent => _.includes(["APPLICATION", "ACTOR", "END_USER_APPLICATION"], ent.entityReference.kind))
        .forEach(ent => newModel.addNode(toGraphNode(ent.entityReference)))
        .value();

    // relationships
    _.chain(entityNodes)
        .filter(ent => _.includes(["MEASURABLE", "CHANGE_INITIATIVE", "DATA_TYPE"], ent.entityReference.kind))
        .forEach(ent => newModel.addRelationship(toGraphNode(ent.entityReference)))
        .value();

    // flows
    _.chain(entityNodes)
        .filter(node => node.entityReference.kind === "LOGICAL_DATA_FLOW")
        .map(node => logicalFlowsById[node.entityReference.id])
        .filter(logicalFlow => logicalFlow != null)
        .map(f => Object.assign(f, {kind: "LOGICAL_DATA_FLOW"}))
        .forEach(f => newModel.addFlow(toGraphFlow(f)))
        .value();

    // decorations (physical flows)
    _.chain(physicalFlows)
        .map(pf => ({
            ref: {
                kind: "LOGICAL_DATA_FLOW",
                id: pf.logicalFlowId
            },
            decoration: {
                kind: "PHYSICAL_FLOW",
                id: pf.id
            }
        }))
        .forEach(d => newModel.addDecoration(d))
        .value();

    // positioning
    _.chain(layoutData.positions)
        .map((p, k) => ({
            id: k,
            dx: p.x,
            dy: p.y
        }))
        .forEach(p => positions.move(p))
        .value();


    _.chain(entityNodes)
        .filter(ent => _.includes(["APPLICATION", "ACTOR"], ent.entityReference.kind))
        .filter(n => {
            const hasPosition = layoutData.positions[toGraphId(n.entityReference)];
            return !hasPosition;
        })
        .forEach(n => positions.move({
            id: toGraphId(n.entityReference),
            dx: Math.random() * 1000,
            dy: Math.random() * 1000}))
        .value()

    // annotations
    _.chain(annotations)
        .map(ann => ({
            kind: "ANNOTATION",
            id: ann.annotationId,
            entityReference: ann.entityReference,
            note: ann.note,
        }))
        .forEach(ann => newModel.addAnnotation(toGraphNode(ann)))
        .value();

    diagramTransform.set(layoutData.diagramTransform);

    dirty.set(false);
}



export function service(
    $q,
    applicationStore,
    flowDiagramStore,
    flowDiagramAnnotationStore,
    flowDiagramEntityStore,
    logicalFlowStore,
    physicalFlowStore,
    measurableRatingStore,
    flowDiagramOverlayGroupStore)
{

    const reset = () => {
        newModel.reset();
        overlay.reset();
        positions.reset();
        dirty.set(false);
    };


    const addMissingEntityNodes = function (diagramId, entityNodes = [], applications = []) {
        const appEntityIds = _
            .chain(entityNodes)
            .filter(e => e.entityReference.kind === "APPLICATION")
            .map(e => e.entityReference.id)
            .value();


        const missingEntityNodes = _
            .chain(applications)
            .filter(a => !_.includes(appEntityIds, a.id))
            .map(a => ({
                diagramId,
                entityReference: toEntityRef(a),
                isNotable: false
            }))
            .value();

        return [...entityNodes, ...missingEntityNodes];
    };

    const load = (id) => {
        const diagramRef = { id: id, kind: "FLOW_DIAGRAM"};
        const diagramSelector = {
            entityReference: diagramRef,
            scope: "EXACT",
            entityLifecycleStatuses: ["ACTIVE", "PENDING", "REMOVED"]
        };

        reset();

        const promises = [
            applicationStore.findBySelector(diagramSelector),
            flowDiagramStore.getById(id),
            flowDiagramAnnotationStore.findByDiagramId(id),
            flowDiagramEntityStore.findByDiagramId(id),
            logicalFlowStore.findBySelector(diagramSelector),
            physicalFlowStore.findBySelector(diagramSelector),
            flowDiagramOverlayGroupStore.findOverlaysByDiagramId(id)
        ];

        return $q
            .all(promises)
            .then(([applications, diagram, annotations, entityNodes, logicalFlows, physicalFlows, overlayEntries]) => {
                if(!_.isNil(diagram)) {

                    const allEntityNodes = addMissingEntityNodes(diagram.id, entityNodes, applications);

                    restoreDiagram(diagram, annotations, allEntityNodes, logicalFlows, physicalFlows);

                    // store on state the list of groups to be shown
                    const overlayPromises = _.map(
                        overlayEntries,
                        overlay => applicationStore
                            .findBySelector(mkSelectionOptions(overlay.entityReference))
                            .then(r => Object.assign(
                                {},
                                overlay,
                                {
                                    kind: "OVERLAY",
                                    groupRef: toGraphId({id: overlay.overlayGroupId, kind: "GROUP"}),
                                    applicationIds: _.map(r, d => d.id)
                                })));

                    $q
                        .all(overlayPromises)
                        .then((overlaysWithApps) => overlaysWithApps.forEach(o => overlay.addOverlay(o)));
                }
            })
            .then(() => dirty.set(false));
    };


    return {
        load,
        reset
    };
}


service.$inject = [
    "$q",
    "ApplicationStore",
    "FlowDiagramStore",
    "FlowDiagramAnnotationStore",
    "FlowDiagramEntityStore",
    "LogicalFlowStore",
    "PhysicalFlowStore",
    "MeasurableRatingStore",
    "FlowDiagramOverlayGroupStore"
];


const serviceName = "FlowDiagramStateService";


export default {
    serviceName,
    service
};
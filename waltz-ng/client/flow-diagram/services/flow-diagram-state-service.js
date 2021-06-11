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


const initialState = {
    dirty: false,
    model: {
        diagramId: null,
        nodes: [],
        flows: [],
        decorations: {},
        annotations: [],
        groups: []
    },
    layout: {
        positions: {}, // gid -> {  x, y }
        shapes: {}, // gid -> { path, cx, cy, etc} }
        diagramTransform: null
    },
    visibility: {
        layers: {
            flowBuckets: true,
            annotations: true,
            pendingFlows: true,
            removedFlows: true
        }
    },
    detail: {
        applicationsById: {},
        measurablesByAppId: {}
    }
};


let state = _.cloneDeep(initialState);


function toRef(d) {
    return {
        kind: d.data.kind,
        id: d.data.id
    };
}


function prepareSaveCmd(state) {
    const nodes = _.map(state.model.nodes, n => {
        return {
            entityReference: toRef(n),
            isNotable: false
        };
    });

    const flows = _.map(state.model.flows, f => {
        return {
            entityReference: toRef(f)
        };
    });

    const decorations = _
        .chain(state.model.decorations)
        .values()
        .flatten()
        .map(d => {
            return {
                entityReference: toRef(d),
                isNotable: false
            };
        })
        .value();

    const entities = _.concat(nodes, flows, decorations);

    const layoutData = {
        positions: state.layout.positions,
        diagramTransform: ifPresent(
            state.layout.diagramTransform,
            x => x.toString(),
            "translate(0,0) scale(1)")
    };

    const annotations = _.map(state.model.annotations, a => {
        const ref = a.data.entityReference;
        return {
            entityReference: { kind: ref.kind, id: ref.id },
            note: a.data.note,
            annotationId: a.data.id
        }
    });

    return {
        diagramId: state.diagramId,
        name: state.model.title,
        description: state.model.description,
        entities,
        annotations,
        layoutData: JSON.stringify(layoutData)
    };
}


function restoreDiagram(
    commandProcessor,
    diagram,
    annotations = [],
    entityNodes = [],
    logicalFlows = [],
    physicalFlows = [],
    overlayGroups = [],
    overlayGroupEntries = [])
{
    const layoutData = JSON.parse(diagram.layoutData);
    const logicalFlowsById = _.keyBy(logicalFlows, "id");

    // nodes
    _.chain(entityNodes)
        .filter(ent => _.includes(["APPLICATION", "ACTOR"], ent.entityReference.kind))
        .forEach(ent => newModel.addNode(toGraphNode(ent.entityReference)))
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

    // overlay groups
    _.chain(overlayGroups)
        .map(g => ({
            id: toGraphId({id: g.id, kind: 'GROUP'}),
            data: g}))
        .forEach(g => overlay.addGroup(g))
        .value();

    //  overlay entries
    _.chain(overlayGroupEntries)
        .map(g => ({
            id: toGraphId({id: g.id, kind: 'OVERLAY'}),
            groupRef: toGraphId({id: g.overlayGroupId, kind: 'GROUP'}),
            data: g}))
        .forEach(g => overlay.addOverlay(g))
        .value();

    diagramTransform.set(layoutData.diagramTransform);

    const titleCommands = [{
        command: "SET_TITLE",
        payload: diagram.name
    }];

    const descriptionCommands = [{
        command: "SET_DESCRIPTION",
        payload: diagram.description
    }];

    // commandProcessor(annotationCommands);
    commandProcessor(titleCommands);
    commandProcessor(descriptionCommands);
    // commandProcessor(groupCommands);
}




function addGroup(payload, model) {
    const group = Object.assign({},
                                payload,
                                {id: toGraphId(payload.group)});

    const currentGroups = model.groups || [];
    const existingIds = _.map(currentGroups, "id");
    if (_.includes(existingIds, group.id)) {
        console.log("Ignoring request to add duplicate overlay");
    } else {
        model.groups = _.concat(currentGroups, [group]);
    }
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
        state = _.cloneDeep(initialState);
        if (listener) listener(state);
    };

    const save = () => {
        const cmd = prepareSaveCmd(state);
        return flowDiagramStore.save(cmd)
            .then(id => {
                state.diagramId = id;
                state.dirty = false;
                dirty.set(false);
                listener(state);
                return id;
            });
    };

    const updateName = () => {
        const cmd = {
            newName: state.model.title
        };
        return flowDiagramStore.updateName(state.diagramId, cmd);
    };

    const updateDescription = () => {
        const cmd = {
            newDescription: state.model.description
        };
        return flowDiagramStore.updateDescription(state.diagramId, cmd);
    };

    const addMissingEntityNodes = function (diagramId, entityNodes = [], applications = []) {
        const appEntityIds = _.chain(entityNodes)
            .filter(e => e.entityReference.kind === "APPLICATION")
            .map(e => e.entityReference.id)
            .value();


        const missingEntityNodes = _.chain(applications)
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
        state.diagramId = id;

        const promises = [
            applicationStore.findBySelector(diagramSelector),
            flowDiagramStore.getById(id),
            flowDiagramAnnotationStore.findByDiagramId(id),
            flowDiagramEntityStore.findByDiagramId(id),
            logicalFlowStore.findBySelector(diagramSelector),
            physicalFlowStore.findBySelector(diagramSelector),
            measurableRatingStore.findByAppSelector(diagramSelector),
            flowDiagramOverlayGroupStore.findByDiagramId(id),
            flowDiagramOverlayGroupStore.findOverlaysByDiagramId(id)
        ];

        return $q
            .all(promises)
            .then(([applications, diagram, annotations, entityNodes, logicalFlows, physicalFlows, measurableRatings, overlayGroups, overlayEntries]) => {
                state.detail.applicationsById = _.keyBy(applications, "id");
                state.detail.measurablesByAppId = _.keyBy(measurableRatings, "entityReference.id")
                const allEntityNodes = addMissingEntityNodes(diagram.id, entityNodes, applications);


                restoreDiagram(processCommands, diagram, annotations, allEntityNodes, logicalFlows, physicalFlows, overlayGroups, overlayEntries);

                // store on state the list of groups to be shown
                const groupPromises = _.map(
                    overlayEntries,
                    overlay => applicationStore
                        .findBySelector(mkSelectionOptions(overlay.entityReference))
                        .then(r => Object.assign({}, overlay, {applicationIds: _.map(r, d => d.id)})));

                $q
                    .all(groupPromises)
                    .then((overlaysWithApps) => overlaysWithApps.forEach(o => overlay.updateOverlay(o)));

                state.dirty = false;
            })
            .then(() => getState());
    };

    const processCommand = (state, commandObject) => {
        // console.log("wFDSS - processing command: ", commandObject, state);
        const payload = commandObject.payload;
        const model = state.model;
        switch (commandObject.command) {
            // case "TRANSFORM_DIAGRAM":
            //     diagramTransform.set(payload);
            //     // state = _.defaultsDeep({}, { layout: { diagramTransform : payload }}, state);
            //     break;

            case "SET_TITLE":
                model.title = payload;
                break;

            case "SET_DESCRIPTION":
                model.description = payload;
                break;

            // /* MOVE
            //     payload = { dx, dy, id, refId? }
            //     - dx = delta x
            //     - dy = delta y
            //     - id = identifier of item to move
            //     - refId? = if specified, move is relative to the current position of this item
            //  */
            // case "MOVE":
            //     positions.move(payload);
            //     // const startPosition = payload.refId
            //     //     ? positionFor(state, payload.refId)
            //     //     : positionFor(state, payload.id);
            //     // const endPosition = positionFor(state, payload.id);
            //     // endPosition.x = startPosition.x + payload.dx;
            //     // endPosition.y = startPosition.y + payload.dy;
            //     break;
            //
            // /* UPDATE_ANNOTATION
            //     payload = { note, id }
            //     - note = text to use in the annotation
            //     - id = annotation identifier
            //  */
            // case "UPDATE_ANNOTATION":
            //     newModel.updateAnnotation(payload);
            //     // model.annotations = _.map(
            //     //     model.annotations,
            //     //     ann =>{
            //     //         if (ann.id !== payload.id) {
            //     //             return ann;
            //     //         } else {
            //     //             const updatedAnn = Object.assign({}, ann);
            //     //             updatedAnn.data.note = payload.note;
            //     //             return updatedAnn;
            //     //         }
            //     //     });
            //     break;
            //
            // case "ADD_ANNOTATION":
            //     const annotationNode = toGraphNode(payload);
            //     newModel.addAnnotation(annotationNode);
            //     // const existingAnnotationIds = _.map(model.annotations, "id");
            //     // if (_.includes(existingAnnotationIds, payload.id)) {
            //     //     console.log("Ignoring request to re-add annotation", payload);
            //     // } else {
            //     //     model.annotations = _.concat(model.annotations || [], [ annotationNode ]);
            //     // }
            //     break;
            //
            // case "ADD_NODE":
            //     newModel.addNode(toGraphNode(payload));
            //     //
            //     // const graphNode = toGraphNode(payload);
            //     // const existingNodeIds = _.map(model.nodes, "id");
            //     // if (_.includes(existingNodeIds, graphNode.id)) {
            //     //     console.log("Ignoring request to re-add node", payload);
            //     // } else {
            //     //     model.nodes = _.concat(model.nodes || [], [ graphNode ]);
            //     //     listener(state);
            //     // }
            //     // if (graphNode.data.kind === "APPLICATION" && !state.detail.applicationsById[graphNode.data.id]) {
            //     //     // load full app detail
            //     //     applicationStore
            //     //         .getById(graphNode.data.id)
            //     //         .then(app => state.detail.applicationsById[graphNode.data.id] = app)
            //     //         .then(() => listener(state));
            //     // }
            //     break;
            //
            // case "ADD_FLOW":
            //     newModel.addFlow(toGraphFlow(payload));
            //     // const graphFlow = toGraphFlow(payload);
            //     // const existingFlowIds = _.map(model.flows, "id");
            //     // if (_.includes(existingFlowIds, graphFlow.id)) {
            //     //     console.log("Ignoring request to add duplicate flow", payload);
            //     // } else {
            //     //     model.flows = _.concat(model.flows || [], [graphFlow]);
            //     // }
            //     break;
            //
            // case "REMOVE_FLOW":
            //     newModel.removeFlow(payload);
            //     // model.annotations = _.reject(
            //     //     model.annotations,
            //     //     a => toGraphId(a.data.entityReference) === payload.id);
            //     // model.flows = _.reject(model.flows, f => f.id === payload.id);
            //     break;
            //
            // case "ADD_DECORATION":
            //     newModel.addDecoration(payload);
            //     break;
            //
            // case "REMOVE_DECORATION":
            //     newModel.removeDecoration(payload);
            //     break;
            //
            // case "REMOVE_NODE":
            //     console.log("REMOVE_NODE", payload)
            //     newModel.removeNode(payload);
            //     // const flowIdsToRemove = _.chain(model.flows)
            //     //     .filter(f => f.source === payload.id || f.target === payload.id)
            //     //     .map("id")
            //     //     .value();
            //     // model.flows = _.reject(model.flows, f => _.includes(flowIdsToRemove, f.id));
            //     // model.nodes = _.reject(model.nodes, n => n.id === payload.id);
            //     // model.annotations = _.reject(
            //     //     model.annotations,
            //     //     a => {
            //     //         const annotationEntityRef = toGraphId(a.data.entityReference);
            //     //         const isDirectAnnotation = annotationEntityRef === payload.id;
            //     //         const isFlowAnnotation = _.includes(flowIdsToRemove, annotationEntityRef);
            //     //         return isDirectAnnotation || isFlowAnnotation;
            //     //     });
            //     // _.forEach(flowIdsToRemove, id => model.decorations[id] = []);
            //     // listener(state);
            //     break;
            //
            // case "REMOVE_ANNOTATION":
            //     newModel.removeAnnotation(payload);
            //     // model.annotations = _.reject(model.annotations, a => a.id === payload.id );
            //     break;

            case "ADD_GROUP":
                addGroup(payload, model);
                break;

            case "REMOVE_GROUP":
                model.groups = _.reject(model.groups, a => a.id === payload.id );
                break;

            case "SET_POSITION":
                positions.setPosition(payload);
                // state.layout.positions[payload.id] = { x: payload.x, y: payload.y };
                break;


            default:
                console.log("WFD: unknown command", commandObject);
                break;
        }
        state.dirty = true;
        return state;
    };

    // used to notify listeners of state changes
    let listener = () => {};

    const processCommands = (commands = []) => {
        state = _.reduce(commands, processCommand, state);
        if (listener) listener(state);
    };


    const getState = () => Object.assign({}, state);

    const onChange = (callback) => listener = callback;

    const getAllEntities = () => {
        const toRef = d => ({ kind: d.data.kind, id: d.data.id });
        const nodes = _.map(state.model.nodes, toRef);
        const flows = _.map(state.model.flows, toRef);
        const decorations = _
            .chain(state.model.decorations)
            .values()
            .flatten()
            .map(toRef)
            .value();

        return _.concat(
            nodes,
            flows,
            decorations);
    };

    const isDirty = () => state.dirty;

    return {
        processCommands,
        getState,
        getAllEntities,
        onChange,
        isDirty,
        save,
        updateName,
        updateDescription,
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
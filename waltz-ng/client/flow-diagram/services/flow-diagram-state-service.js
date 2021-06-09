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
import {positionFor, toGraphFlow, toGraphId, toGraphNode} from "../flow-diagram-utils";
import {toEntityRef} from "../../common/entity-utils";
import {mkSelectionOptions} from "../../common/selector-utils";
import newModel from "../components/diagram-svelte/store/model"
import {diagramTransform} from "../components/diagram-svelte/store/layout";


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
    groups = [])
{
    const layoutData = JSON.parse(diagram.layoutData);
    const logicalFlowsById = _.keyBy(logicalFlows, "id");
    const flowCommands = _
        .chain(entityNodes)
        .filter(node => node.entityReference.kind === "LOGICAL_DATA_FLOW")
        .map(node => logicalFlowsById[node.entityReference.id])
        .filter(logicalFlow => logicalFlow != null)
        .map(logicalFlow => {
            return {
                command: "ADD_FLOW",
                payload: Object.assign({}, logicalFlow, {kind: "LOGICAL_DATA_FLOW"} )
            };
        })
        .value();

    const nodeCommands = _
        .chain(entityNodes)
        .filter(ent => _.includes(["APPLICATION", "ACTOR"], ent.entityReference.kind))
        .forEach(ent => newModel.addNode(toGraphNode(ent.entityReference)))
        .value();


    const entityRefs = _.map(entityNodes, n => toGraphId(n.entityReference));

    const annotationCommands = _
        .chain(annotations)
        .filter(ann => {
            const annotatedEntityExists = ann.entityReference.kind === "LOGICAL_DATA_FLOW"
                ? logicalFlowsById[ann.entityReference.id] !== null
                : _.includes(entityRefs, toGraphId(ann.entityReference));

            if (! annotatedEntityExists) {
                console.warn("wfdss: WARN - cannot attach annotation to missing element: ", ann)
            }
            return annotatedEntityExists;
        })
        .map(ann => {
            return {
                command: "ADD_ANNOTATION",
                payload: {
                    kind: "ANNOTATION",
                    id: ann.annotationId,
                    entityReference: ann.entityReference,
                    note: ann.note,
                }
            };
        })
        .value();

    const decorationCommands = _.map(physicalFlows, pf => {
        return {
            command: "ADD_DECORATION",
            payload: {
                ref: {
                    kind: "LOGICAL_DATA_FLOW",
                    id: pf.logicalFlowId
                },
                decoration: {
                    kind: "PHYSICAL_FLOW",
                    id: pf.id
                }
            }
        }
    });

    const moveCommands = _.map(
        layoutData.positions,
        (p, k) => {
            return {
                command: "MOVE",
                payload: {
                    id: k,
                    dx: p.x,
                    dy: p.y
                }
            }
        });

    const transformCommands = [{
        command: "TRANSFORM_DIAGRAM",
        payload: layoutData.diagramTransform
    }];

    const titleCommands = [{
        command: "SET_TITLE",
        payload: diagram.name
    }];

    const descriptionCommands = [{
        command: "SET_DESCRIPTION",
        payload: diagram.description
    }];

    const groupCommands = _.map(groups, g => {
        return {
            command: "ADD_GROUP",
            payload: g
        }
    });

    // commandProcessor(nodeCommands);
    commandProcessor(flowCommands);
    commandProcessor(annotationCommands);
    commandProcessor(moveCommands);
    commandProcessor(transformCommands);
    commandProcessor(decorationCommands);
    commandProcessor(titleCommands);
    commandProcessor(descriptionCommands);
    commandProcessor(groupCommands);
}


function addDecoration(payload, model) {
    const refId = toGraphId(payload.ref);
    const decorationNode = toGraphNode(payload.decoration);
    const currentDecorations = model.decorations[refId] || [];
    const existingIds = _.map(currentDecorations, "id");
    if (_.includes(existingIds, decorationNode.id)) {
        console.log("Ignoring request to add duplicate decoration");
    } else {
        model.decorations[refId] = _.concat(currentDecorations, [decorationNode]);
    }
}


function removeDecoration(payload, model) {
    const refOfRemovedDecoration = toGraphId(payload.ref);
    const decorationNodeToRemove = toGraphNode(payload.decoration);
    const currentDecorations = model.decorations[refOfRemovedDecoration] || [];
    const existingIds = _.map(currentDecorations, "id");
    if (_.includes(existingIds, decorationNodeToRemove.id)) {
        model.decorations[refOfRemovedDecoration] = _.reject(
            currentDecorations,
            d => d.id === decorationNodeToRemove.id);
    } else {
        console.log("Ignoring request to removed unknown decoration");
    }
}


function addGroup(payload, model) {
    const group = Object.assign({},
                                payload,
                                {id: toGraphId(payload.group)});

    const currentGroups = model.groups || [];
    const existingIds = _.map(currentGroups, "id");
    if (_.includes(existingIds, group.id)) {
        console.log("Ignoring request to add duplicate group");
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
    measurableRatingStore)
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
            measurableRatingStore.findByAppSelector(diagramSelector)
        ];

        return $q
            .all(promises)
            .then(([applications, diagram, annotations, entityNodes, logicalFlows, physicalFlows, measurableRatings]) => {
                state.detail.applicationsById = _.keyBy(applications, "id");
                state.detail.measurablesByAppId = _.keyBy(measurableRatings, "entityReference.id")
                const allEntityNodes = addMissingEntityNodes(diagram.id, entityNodes, applications);


                restoreDiagram(processCommands, diagram, annotations, allEntityNodes, logicalFlows, physicalFlows);

                //store on state the list of groups to be shown
                const groupPromises = _.map(
                    state.model.groups,
                    g => applicationStore
                        .findBySelector(mkSelectionOptions(g.group.entityReference))
                        .then(r => Object.assign({}, {group: g.group, applicationIds: _.map(r, d => d.id)})));

                $q
                    .all(groupPromises)
                    .then((groupIdsWithApps) => state.model.groups = groupIdsWithApps)

                state.dirty = false;
            })
            .then(() => getState());
    };

    const processCommand = (state, commandObject) => {
        // console.log("wFDSS - processing command: ", commandObject, state);
        const payload = commandObject.payload;
        const model = state.model;
        switch (commandObject.command) {
            case "TRANSFORM_DIAGRAM":
                diagramTransform.set(payload);
                // state = _.defaultsDeep({}, { layout: { diagramTransform : payload }}, state);
                break;

            case "SET_TITLE":
                model.title = payload;
                break;

            case "SET_DESCRIPTION":
                model.description = payload;
                break;

            /* MOVE
                payload = { dx, dy, id, refId? }
                - dx = delta x
                - dy = delta y
                - id = identifier of item to move
                - refId? = if specified, move is relative to the current position of this item
             */
            case "MOVE":
                const startPosition = payload.refId
                    ? positionFor(state, payload.refId)
                    : positionFor(state, payload.id);
                const endPosition = positionFor(state, payload.id);
                endPosition.x = startPosition.x + payload.dx;
                endPosition.y = startPosition.y + payload.dy;
                break;

            /* UPDATE_ANNOTATION
                payload = { note, id }
                - note = text to use in the annotation
                - id = annotation identifier
             */
            case "UPDATE_ANNOTATION":
                newModel.updateAnnotation(payload);
                // model.annotations = _.map(
                //     model.annotations,
                //     ann =>{
                //         if (ann.id !== payload.id) {
                //             return ann;
                //         } else {
                //             const updatedAnn = Object.assign({}, ann);
                //             updatedAnn.data.note = payload.note;
                //             return updatedAnn;
                //         }
                //     });
                break;

            case "ADD_ANNOTATION":
                const annotationNode = toGraphNode(payload);
                newModel.addAnnotation(annotationNode);
                // const existingAnnotationIds = _.map(model.annotations, "id");
                // if (_.includes(existingAnnotationIds, payload.id)) {
                //     console.log("Ignoring request to re-add annotation", payload);
                // } else {
                //     model.annotations = _.concat(model.annotations || [], [ annotationNode ]);
                // }
                break;

            case "ADD_NODE":
                newModel.addNode(toGraphNode(payload));
                //
                // const graphNode = toGraphNode(payload);
                // const existingNodeIds = _.map(model.nodes, "id");
                // if (_.includes(existingNodeIds, graphNode.id)) {
                //     console.log("Ignoring request to re-add node", payload);
                // } else {
                //     model.nodes = _.concat(model.nodes || [], [ graphNode ]);
                //     listener(state);
                // }
                // if (graphNode.data.kind === "APPLICATION" && !state.detail.applicationsById[graphNode.data.id]) {
                //     // load full app detail
                //     applicationStore
                //         .getById(graphNode.data.id)
                //         .then(app => state.detail.applicationsById[graphNode.data.id] = app)
                //         .then(() => listener(state));
                // }
                break;

            case "ADD_FLOW":
                newModel.addFlow(toGraphFlow(payload));
                // const graphFlow = toGraphFlow(payload);
                // const existingFlowIds = _.map(model.flows, "id");
                // if (_.includes(existingFlowIds, graphFlow.id)) {
                //     console.log("Ignoring request to add duplicate flow", payload);
                // } else {
                //     model.flows = _.concat(model.flows || [], [graphFlow]);
                // }
                break;

            case "REMOVE_FLOW":
                newModel.removeFlow(payload);
                // model.annotations = _.reject(
                //     model.annotations,
                //     a => toGraphId(a.data.entityReference) === payload.id);
                // model.flows = _.reject(model.flows, f => f.id === payload.id);
                break;

            case "ADD_DECORATION":
                addDecoration(payload, model);
                break;

            case "REMOVE_DECORATION":
                removeDecoration(payload, model);
                break;

            case "REMOVE_NODE":
                console.log("REMOVE_NODE", payload)
                newModel.removeNode(payload);
                // const flowIdsToRemove = _.chain(model.flows)
                //     .filter(f => f.source === payload.id || f.target === payload.id)
                //     .map("id")
                //     .value();
                // model.flows = _.reject(model.flows, f => _.includes(flowIdsToRemove, f.id));
                // model.nodes = _.reject(model.nodes, n => n.id === payload.id);
                // model.annotations = _.reject(
                //     model.annotations,
                //     a => {
                //         const annotationEntityRef = toGraphId(a.data.entityReference);
                //         const isDirectAnnotation = annotationEntityRef === payload.id;
                //         const isFlowAnnotation = _.includes(flowIdsToRemove, annotationEntityRef);
                //         return isDirectAnnotation || isFlowAnnotation;
                //     });
                // _.forEach(flowIdsToRemove, id => model.decorations[id] = []);
                // listener(state);
                break;

            case "REMOVE_ANNOTATION":
                newModel.removeAnnotation(payload);
                // model.annotations = _.reject(model.annotations, a => a.id === payload.id );
                break;

            case "ADD_GROUP":
                addGroup(payload, model);
                break;

            case "REMOVE_GROUP":
                model.groups = _.reject(model.groups, a => a.id === payload.id );
                break;

            case "SET_POSITION":
                state.layout.positions[payload.id] = { x: payload.x, y: payload.y };
                break;

            case "SHOW_LAYER":
                state.visibility.layers[payload] = true;
                break;

            case "HIDE_LAYER":
                state.visibility.layers[payload] = false;
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
    "MeasurableRatingStore"
];


const serviceName = "FlowDiagramStateService";


export default {
    serviceName,
    service
};
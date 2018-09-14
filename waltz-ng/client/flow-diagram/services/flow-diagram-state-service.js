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
import {ifPresent} from "../../common";
import {positionFor, toGraphFlow, toGraphId, toGraphNode} from "../flow-diagram-utils";


const initialState = {
    dirty: false,
    model: {
        diagramId: null,
        nodes: [],
        flows: [],
        decorations: {},
        annotations: []
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
        applicationsById: {}
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
    physicalFlows = [])
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
        .map(ent => {
            return {
                command: "ADD_NODE",
                payload: {
                    id: ent.entityReference.id,
                    kind: ent.entityReference.kind,
                    name: ent.entityReference.name,
                    isNotable: ent.isNotable
                }
            }
        })
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

    commandProcessor(nodeCommands);
    commandProcessor(flowCommands);
    commandProcessor(annotationCommands);
    commandProcessor(moveCommands);
    commandProcessor(transformCommands);
    commandProcessor(decorationCommands);
    commandProcessor(titleCommands);
    commandProcessor(descriptionCommands);
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


export function service(
    $q,
    applicationStore,
    flowDiagramStore,
    flowDiagramAnnotationStore,
    flowDiagramEntityStore,
    logicalFlowStore,
    physicalFlowStore)
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
            physicalFlowStore.findBySelector(diagramSelector)
        ];

        return $q
            .all(promises)
            .then(([applications, diagram, annotations, entityNodes, logicalFlows, physicalFlows]) => {
                state.detail.applicationsById = _.keyBy(applications, "id");
                restoreDiagram(processCommands, diagram, annotations, entityNodes, logicalFlows, physicalFlows);
                state.dirty = false;
            })
            .then(() => getState());
    };

    const processCommand = (state, commandObject) => {
        //console.log("wFDSS - processing command: ", commandObject, state);
        const payload = commandObject.payload;
        const model = state.model;
        switch (commandObject.command) {
            case "TRANSFORM_DIAGRAM":
                state = _.defaultsDeep({}, { layout: { diagramTransform : payload }}, state);
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
                model.annotations = _.map(
                    model.annotations,
                    ann =>{
                        if (ann.id !== payload.id) {
                            return ann;
                        } else {
                            const updatedAnn = Object.assign({}, ann);
                            updatedAnn.data.note = payload.note;
                            return updatedAnn;
                        }
                    });
                break;

            case "ADD_ANNOTATION":
                const annotationNode = toGraphNode(payload);
                const existingAnnotationIds = _.map(model.annotations, "id");
                if (_.includes(existingAnnotationIds, payload.id)) {
                    console.log("Ignoring request to re-add annotation", payload);
                } else {
                    model.annotations = _.concat(model.annotations || [], [ annotationNode ]);
                }
                break;

            case "ADD_NODE":
                const graphNode = toGraphNode(payload);
                const existingNodeIds = _.map(model.nodes, "id");
                if (_.includes(existingNodeIds, graphNode.id)) {
                    console.log("Ignoring request to re-add node", payload);
                } else {
                    model.nodes = _.concat(model.nodes || [], [ graphNode ]);
                    listener(state);
                }
                if (graphNode.data.kind === "APPLICATION" && !state.detail.applicationsById[graphNode.data.id]) {
                    // load full app detail
                    applicationStore
                        .getById(graphNode.data.id)
                        .then(app => state.detail.applicationsById[graphNode.data.id] = app)
                        .then(() => listener(state));
                }
                break;

            case "ADD_FLOW":
                const graphFlow = toGraphFlow(payload);
                const existingFlowIds = _.map(model.flows, "id");
                if (_.includes(existingFlowIds, graphFlow.id)) {
                    console.log("Ignoring request to add duplicate flow", payload);
                } else {
                    model.flows = _.concat(model.flows || [], [graphFlow]);
                }
                break;

            case "REMOVE_FLOW":
                model.annotations = _.reject(
                    model.annotations,
                    a => toGraphId(a.data.entityReference) === payload.id);
                model.flows = _.reject(model.flows, f => f.id === payload.id);
                break;

            case "ADD_DECORATION":
                addDecoration(payload, model);
                break;

            case "REMOVE_DECORATION":
                removeDecoration(payload, model);
                break;

            case "REMOVE_NODE":
                const flowIdsToRemove = _.chain(model.flows)
                    .filter(f => f.source === payload.id || f.target === payload.id)
                    .map("id")
                    .value();
                model.flows = _.reject(model.flows, f => _.includes(flowIdsToRemove, f.id));
                model.nodes = _.reject(model.nodes, n => n.id === payload.id);
                model.annotations = _.reject(
                    model.annotations,
                    a => {
                        const annotationEntityRef = toGraphId(a.data.entityReference);
                        const isDirectAnnotation = annotationEntityRef === payload.id;
                        const isFlowAnnotation = _.includes(flowIdsToRemove, annotationEntityRef);
                        return isDirectAnnotation || isFlowAnnotation;
                    });
                _.forEach(flowIdsToRemove, id => model.decorations[id] = []);
                break;

            case "REMOVE_ANNOTATION":
                model.annotations = _.reject(model.annotations, a => a.id === payload.id );
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
    "PhysicalFlowStore"
];


const serviceName = "FlowDiagramStateService";


export default {
    serviceName,
    service
};
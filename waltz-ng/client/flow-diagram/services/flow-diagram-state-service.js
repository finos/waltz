/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016  Khartec Ltd.
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
import _ from 'lodash';
import {ifPresent} from '../../common';
import {positionFor, toGraphFlow, toGraphId, toGraphNode} from '../flow-diagram-utils';


const initialState = {
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
        description: '',
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
    const logicalFlowsById = _.keyBy(logicalFlows, 'id');
    const flowCommands = _
        .chain(entityNodes)
        .filter(node => node.entityReference.kind === 'LOGICAL_DATA_FLOW')
        .map(node => {
            const logicalFlow = logicalFlowsById[node.entityReference.id];
            return {
                command: 'ADD_FLOW',
                payload: Object.assign({}, logicalFlow, {kind: 'LOGICAL_DATA_FLOW'} )
            }
        })
        .value();

    const nodeCommands = _
        .chain(entityNodes)
        .filter(ent => _.includes(['APPLICATION', 'ACTOR'], ent.entityReference.kind))
        .map(ent => {
            return {
                command: 'ADD_NODE',
                payload: {
                    id: ent.entityReference.id,
                    kind: ent.entityReference.kind,
                    name: ent.entityReference.name,
                    isNotable: ent.isNotable
                }
            }
        })
        .value();

    const annotationCommands = _.map(annotations, ann => {
        return {
            command: 'ADD_ANNOTATION',
            payload: {
                kind: 'ANNOTATION',
                id: ann.annotationId,
                entityReference: ann.entityReference,
                note: ann.note,
            }
        }
    });

    const decorationCommands = _.map(physicalFlows, pf => {
        return {
            command: 'ADD_DECORATION',
            payload: {
                ref: {
                    kind: 'LOGICAL_DATA_FLOW',
                    id: pf.logicalFlowId
                },
                decoration: {
                    kind: 'PHYSICAL_FLOW',
                    id: pf.id
                }
            }
        }
    });

    const moveCommands = _.map(
        layoutData.positions,
        (p, k) => {
            return {
                command: 'MOVE',
                payload: {
                    id: k,
                    dx: p.x,
                    dy: p.y
                }
            }
        });

    const transformCommands = [{
        command: 'TRANSFORM_DIAGRAM',
        payload: layoutData.diagramTransform
    }];

    commandProcessor(nodeCommands);
    commandProcessor(flowCommands);
    commandProcessor(annotationCommands);
    commandProcessor(moveCommands);
    commandProcessor(transformCommands);
    commandProcessor(decorationCommands);
}


function service(
    $q,
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
            .then(id => state.diagramId = id)
    };

    const load = (id) => {
        const diagramRef = { id: id, kind: 'FLOW_DIAGRAM'};
        const diagramSelector = { entityReference: diagramRef, scope: 'EXACT' };

        reset();
        state.diagramId = id;

        const diagramPromise = flowDiagramStore.getById(id);
        const annotationPromise =flowDiagramAnnotationStore.findByDiagramId(id);
        const entityPromise = flowDiagramEntityStore.findByDiagramId(id);
        const logicalFlowPromise = logicalFlowStore.findBySelector(diagramSelector);
        const physicalFlowPromise = physicalFlowStore.findBySelector(diagramSelector);

        return $q
            .all([diagramPromise, annotationPromise, entityPromise, logicalFlowPromise, physicalFlowPromise])
            .then(([diagram, annotations, entityNodes, logicalFlows, physicalFlows]) => {
                restoreDiagram(processCommands, diagram, annotations, entityNodes, logicalFlows, physicalFlows);
            });
    };

    const processCommand = (state, commandObject) => {
        console.log("wFD - processing command: ", commandObject, state);
        const payload = commandObject.payload;
        const model = state.model;
        switch (commandObject.command) {
            case 'TRANSFORM_DIAGRAM':
                state = _.defaultsDeep({}, { layout: { diagramTransform : payload }}, state);
                break;

            case 'SET_TITLE':
                model.title = payload;
                break;

            /* MOVE
                payload = { dx, dy, id, refId? }
                - dx = delta x
                - dy = delta y
                - id = identifier of item to move
                - refId? = if specified, move is relative to the current position of this item
             */
            case 'MOVE':
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
            case 'UPDATE_ANNOTATION':
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

            case 'ADD_ANNOTATION':
                const annotationNode = toGraphNode(payload);
                const existingIds = _.map(model.annotations, "id");
                if (_.includes(existingIds, payload.id)) {
                    console.log('Ignoring request to re-add annotation', payload);
                } else {
                    model.annotations = _.concat(model.annotations || [], [ annotationNode ]);
                }
                break;

            case 'ADD_NODE':
                const graphNode = toGraphNode(payload);
                const existingIds = _.map(model.nodes, "id");
                if (_.includes(existingIds, graphNode.id)) {
                    console.log('Ignoring request to re-add node', payload);
                } else {
                    model.nodes = _.concat(model.nodes || [], [ graphNode ]);
                    listener(state)
                }
                break;

            case 'ADD_FLOW':
                const graphFlow = toGraphFlow(payload);
                const existingIds = _.map(model.flows, "id");
                if (_.includes(existingIds, graphFlow.id)) {
                    console.log('Ignoring request to add duplicate flow', payload);
                } else {
                    model.flows = _.concat(model.flows || [], [graphFlow]);
                }
                break;

            case 'REMOVE_FLOW':
                model.annotations = _.reject(
                    model.annotations,
                    a => toGraphId(a.data.entityReference) === payload.id);
                model.flows = _.reject(model.flows, f => f.id === payload.id);
                break;

            case 'ADD_DECORATION':
                const payload = payload;
                const refId = toGraphId(payload.ref);
                const decorationNode = toGraphNode(payload.decoration);
                const currentDecorations = model.decorations[refId] || [];
                const existingIds = _.map(currentDecorations, "id");
                if (_.includes(existingIds, decorationNode.id)) {
                    console.log('Ignoring request to add duplicate decoration');
                } else {
                    model.decorations[refId] = _.concat(currentDecorations, [decorationNode]);
                }
                break;

            case 'REMOVE_DECORATION':
                const payload = payload;
                const refId = toGraphId(payload.ref);
                const decorationNode = toGraphNode(payload.decoration);
                const currentDecorations = model.decorations[refId] || [];
                const existingIds = _.map(currentDecorations, "id");
                if (_.includes(existingIds, decorationNode.id)) {
                    model.decorations[refId] = _.reject(currentDecorations, d => d.id === decorationNode.id);
                } else {
                    console.log('Ignoring request to removed unknown decoration');
                }
                break;

            case 'REMOVE_NODE':
                const flowIdsToRemove = _.chain(model.flows)
                    .filter(f => f.source === payload.id || f.target === payload.id)
                    .map('id')
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

            case 'REMOVE_ANNOTATION':
                model.annotations = _.reject(model.annotations, a => a.id === payload.id );
                break;

            case 'SET_POSITION':
                state.layout.positions[payload.id] = { x: payload.x, y: payload.y };
                break;

            default:
                console.log('WFD: unknown command', commandObject);
                break;
        }
        return state;
    };

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

    return {
        processCommands,
        getState,
        getAllEntities,
        onChange,
        save,
        load,
        reset
    };
}


service.$inject = [
    '$q',
    'FlowDiagramStore',
    'FlowDiagramAnnotationStore',
    'FlowDiagramEntityStore',
    'LogicalFlowStore',
    'PhysicalFlowStore'
];


export default service;
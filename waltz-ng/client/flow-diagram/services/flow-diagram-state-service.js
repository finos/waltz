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
import {randomPick, ifPresent} from '../../common';
import {toGraphFlow, toGraphNode, toGraphId, positionFor} from '../flow-diagram-utils';


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


function prepareSaveCmd(state) {
    const nodes = _.map(state.model.nodes, n => {
        return {
            entityReference: {
                id: n.data.id,
                kind: n.data.kind
            },
            isNotable: false
        };
    });

    const flows = _.map(state.model.flows, f => {
        return {
            entityReference: {
                kind: f.data.kind,
                id: f.data.id
            }
        };
    });

    const entities = _.concat(nodes, flows);

    const layoutData = {
        positions: state.layout.positions,
        diagramTransform: ifPresent(
            state.layout.diagramTransform,
            x => x.toString(),
            "translate(0,0) scale(0)")
    };

    const annotations = _.map(state.model.annotations, a => {
        return {
            entityReference: a.data.entityReference,
            note: a.data.note,
            annotationId: a.data.id
        }
    });

    return {
        diagramId: state.diagramId,
        name: 'Test',
        description: 'Test Diagram',
        entities,
        annotations,
        layoutData: JSON.stringify(layoutData)
    };
}


function loadDiagram(commandProcessor, diagram, annotations, entityNodes, logicalFlows) {
    const layoutData = JSON.parse(diagram.layoutData);
    const logicalFlowsById = _.keyBy(logicalFlows, 'id');
    const mkFlowCommands = _
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

    const mkNodeCommands = _
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

    const mkAnnotationCommands = _.map(annotations, ann => {
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

    commandProcessor(mkNodeCommands);
    commandProcessor(mkFlowCommands);
    commandProcessor(mkAnnotationCommands);
    commandProcessor(moveCommands);
    commandProcessor(transformCommands);
}


function service(
    $q,
    flowDiagramStore,
    flowDiagramAnnotationStore,
    flowDiagramEntityStore,
    logicalFlowStore)
{

    const reset = () => {
        state = _.cloneDeep(initialState);
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

        return $q
            .all([diagramPromise, annotationPromise, entityPromise, logicalFlowPromise])
            .then(([diagram, annotations, entityNodes, logicalFlows]) => {
                loadDiagram(processCommands, diagram, annotations, entityNodes, logicalFlows);
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
                const newNote = payload.note;
                const payloadId = payload.id;
                const annotationNode = _.find(model.annotations, {id: payloadId})
                annotationNode.data.note = newNote;
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

            case 'ADD_DECORATION':
                const payload = payload;
                const refId = toGraphId(payload.ref);
                const decorationNode = toGraphNode(payload.decoration);
                const currentDecorations = model.decorations[refId] || [];
                const existingIds = _.map(model.flows, "id");
                if (_.includes(existingIds, decorationNode.id)) {
                    console.log('Ignoring request to add duplicate decoration');
                } else {
                    model.decorations[refId] = _.concat(currentDecorations, [decorationNode]);
                }
                break;

            case 'REMOVE_NODE':
                const flowIdsToRemove = _.chain(model.flows)
                    .filter(f => f.source === payload.id || f.target === payload.id)
                    .map('id')
                    .value();
                model.flows = _.reject(model.flows, f => _.includes(flowIdsToRemove, f.id));
                model.nodes = _.reject(model.nodes, n => n.id === payload.id);
                model.annotations = _.reject(model.annotations, a => {
                    return toGraphId(a.data.entityReference) === payload.id;
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


    return {
        processCommands,
        getState,
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
    'LogicalFlowStore'
];


export default service;
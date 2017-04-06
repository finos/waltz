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
import {randomPick} from '../../common';
import {toGraphFlow, toGraphNode, toGraphId, positionFor} from '../flow-diagram-utils';


let state = {
    model: {
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




function service() {

    const processCommand = (state, commandObject) => {
        console.log("wFD - processing command: ", commandObject, state);
        const payload = commandObject.payload;
        const model = state.model;
        switch (commandObject.command) {
            case 'TRANSFORM_DIAGRAM':
                state = _.defaultsDeep({}, { layout: { diagramTransform : payload }}, state);
                break;

            case 'MOVE':
                const position = positionFor(state, payload);
                position.x += payload.dx;
                position.y += payload.dy;
                break;

            // case 'MOVE_ANNOTATION':
            //     const position = positionFor(state, payload.id);
            //     position.x += payload.dx;
            //     position.y += payload.dy;
            //     break;

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
                    const pos = positionFor(state, annotationNode);
                    pos.x = randomPick([_.random(30, 70), _.random(-30, -70)]);
                    pos.y = randomPick([_.random(30, 70), _.random(-30, -70)]);
                }
                break;

            case 'ADD_NODE':
                const graphNode = toGraphNode(payload);
                const existingIds = _.map(model.nodes, "id");
                if (_.includes(existingIds, graphNode.id)) {
                    console.log('Ignoring request to re-add node', payload);
                } else {
                    model.nodes = _.concat(model.nodes || [], [ graphNode ]);
                    const moveCmd = {
                        graphNode,
                        dx: _.random(100, 600),
                        dy: _.random(100, 400)
                    };
                    processCommands([
                        { command: 'MOVE_NODE', payload: moveCmd}
                    ]);
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
        onChange
    }
}

service.$inject = [
];


export default service;
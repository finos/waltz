/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2017  Waltz open source project
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

import _ from 'lodash';
import {CORE_API} from "../../common/services/core-api-utils";
import {toGraphId} from "../flow-diagram-utils";


export function service($q, serviceBroker) {

    const mkForActor = (entityRef) => {
        return serviceBroker
            .loadViewData(CORE_API.ActorStore.getById, [entityRef.id])
            .then(r => {
                const actor = Object.assign({}, r.data, { kind: 'ACTOR' });
                const title = `${actor.name} flows`;
                const annotation = {
                    id: +new Date()+'',
                    kind: 'ANNOTATION',
                    entityReference: actor,
                    note: `${actor.name} data flows`
                };

                const modelCommands = [
                    { command: 'ADD_NODE', payload: actor },
                    { command: 'ADD_ANNOTATION', payload: annotation },
                    { command: 'SET_TITLE', payload: title }
                ];

                const moveCommands = [
                    { command: 'MOVE', payload: { id: `ANNOTATION/${annotation.id}`, dx: 100, dy: -50 }},
                    { command: 'MOVE', payload: { id: toGraphId(actor), dx: 300, dy: 200 }},
                ];

                return _.concat(modelCommands, moveCommands);
            });
    };

    const mkForApp = (entityRef) => {
        return serviceBroker
            .loadViewData(
                CORE_API.ApplicationStore.getById,
                [ entityRef.id ])
            .then(r => {
                const app  = Object.assign({}, r.data, entityRef)
                const title = `${app.name} flows`;
                const annotation = {
                    id: +new Date()+'',
                    kind: 'ANNOTATION',
                    entityReference: app,
                    note: `${app.name} data flows`
                };

                const modelCommands = [
                    { command: 'ADD_NODE', payload: app },
                    { command: 'ADD_ANNOTATION', payload: annotation },
                    { command: 'SET_TITLE', payload: title }
                ];

                const moveCommands = [
                    { command: 'MOVE', payload: { id: `ANNOTATION/${annotation.id}`, dx: 100, dy: -50 }},
                    { command: 'MOVE', payload: { id: toGraphId(app), dx: 300, dy: 200 }},
                ];

                return _.concat(modelCommands, moveCommands);
            });
    };


    const mkForPhysSpec = (entityRef) => {
        const acc = {
            logicalFlows: [],
            physicalFlows: [],
            specification: null
        };
        return serviceBroker
            .loadViewData(CORE_API.PhysicalSpecificationStore.getById, [entityRef.id])
            .then(r => acc.specification = r.data)
            .then(() => serviceBroker.loadViewData(CORE_API.LogicalFlowStore.findBySelector, [ { entityReference: entityRef, scope: 'EXACT'}]))
            .then(r => acc.logicalFlows = r.data)
            .then(() => serviceBroker.loadViewData(CORE_API.PhysicalFlowStore.findBySpecificationId, [entityRef.id]))
            .then(r => acc.physicalFlows = r.data)
            .then(() => {
                // get log flows
                const nodeCommands = _
                    .chain(acc.logicalFlows)
                    .map(f => { return [f.source, f.target]; })
                    .flatten()
                    .uniqBy(toGraphId)
                    .map(a => ({ command: 'ADD_NODE', payload: a }))
                    .value();

                const flowCommands = _.map(
                    acc.logicalFlows,
                    f => ({ command: 'ADD_FLOW', payload: Object.assign({}, f, { kind: 'LOGICAL_DATA_FLOW' } )}));


                const moveCommands = _.map(
                    nodeCommands,
                    (nc, idx) => {
                        return {
                            command: 'MOVE',
                            payload: {
                                id : toGraphId(nc.payload),
                                dx: 50 + (110 * (idx % 8)),
                                dy: 10 + (50 * (idx / 8))
                            }
                        };
                    });


                const physFlowCommands = _.map(
                    acc.physicalFlows,
                    pf => {
                        return {
                            command: 'ADD_DECORATION',
                            payload: {
                                ref: { kind: 'LOGICAL_DATA_FLOW', id: pf.logicalFlowId },
                                decoration: Object.assign({}, pf, { kind: 'PHYSICAL_FLOW' })
                            }
                        };
                    });

                const title = `${acc.specification.name} Flows`;

                const titleCommands = [
                    { command: 'SET_TITLE', payload: title }
                ];

                return _.concat(nodeCommands, flowCommands, physFlowCommands, moveCommands, titleCommands);
            });
    };

    const mkForPhysFlow = (entityRef) => {
        const acc = {
            physicalFlow: {},
            logicalFlow: {},
            specification: {}
        };

        return serviceBroker
            .loadViewData(CORE_API.PhysicalFlowStore.getById, [ entityRef.id ])
            .then(r => Object.assign(acc.physicalFlow, r.data, {kind: 'PHYSICAL_FLOW'}))
            .then(() => serviceBroker.loadViewData(CORE_API.LogicalFlowStore.getById, [ acc.physicalFlow.logicalFlowId ]))
            .then(r => Object.assign(acc.logicalFlow, r.data, {kind: 'LOGICAL_DATA_FLOW'}))
            .then(() => serviceBroker.loadViewData(CORE_API.PhysicalSpecificationStore.getById, [ acc.physicalFlow.specificationId ]))
            .then(r => Object.assign(acc.specification, r.data, {kind: 'PHYSICAL_SPECIFICATION'}))
            .then(() => {
                const source = Object.assign({}, acc.logicalFlow.source, { isNotable: true });
                const target = Object.assign({}, acc.logicalFlow.target, { isNotable: true });
                const title = `${source.name} sends ${acc.specification.name} to ${target.name}`;
                const annotation = {
                    id: +new Date()+'',
                    kind: 'ANNOTATION',
                    entityReference: acc.logicalFlow,
                    note: `${acc.specification.name} is sent ${acc.physicalFlow.frequency} via ${acc.physicalFlow.transport}`
                };

                const modelCommands = [
                    { command: 'ADD_NODE', payload: source },
                    { command: 'ADD_NODE', payload: target },
                    { command: 'ADD_FLOW', payload: acc.logicalFlow },
                    { command: 'ADD_DECORATION', payload: { ref: acc.logicalFlow, decoration: acc.physicalFlow }},
                    { command: 'ADD_ANNOTATION', payload: annotation },
                    { command: 'SET_TITLE', payload: title }
                ];

                const moveCommands = [
                    { command: 'MOVE', payload: { id: `ANNOTATION/${annotation.id}`, dx: 100, dy: -50 }},
                    { command: 'MOVE', payload: { id: toGraphId(source), dx: 300, dy: 200 }},
                    { command: 'MOVE', payload: { id: toGraphId(target), dx: 400, dy: 300 }},
                ];
                return _.concat(modelCommands, moveCommands);
            });
    };

    const mkCommands = (entityRef) => {
        switch(entityRef.kind) {
            case 'ACTOR':
                return mkForActor(entityRef);
            case 'APPLICATION':
                return mkForApp(entityRef);
            case 'PHYSICAL_FLOW':
                return mkForPhysFlow(entityRef);
            case 'PHYSICAL_SPECIFICATION':
                return mkForPhysSpec(entityRef);
            default:
                console.warn("Don't know how to create starter flow diagram commands for entity ref", entityRef);
                return Promise.resolve([
                    { command: 'SET_TITLE', payload: `${entityRef.name} flows` }
                ]);
        }
    };

    return {
        mkCommands
    };
}


service.$inject = [
    '$q',
    'ServiceBroker'
];


export const serviceName = 'FlowDiagramStarterService';



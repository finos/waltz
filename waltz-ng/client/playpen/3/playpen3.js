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


const initialState = {
    costs: []
};


function controller($stateParams, $timeout) {
    const vm = Object.assign(this, initialState);

    vm.nodes = [
        { kind: 'APPLICATION', id: 12, name: "ian" },
        { kind: 'APPLICATION', id: 13, name: "ivy" }
    ];

    vm.flows = [
        {
            kind: 'LOGICAL_FLOW',
            id: 40,
            source: { kind: 'APPLICATION', id: 12 },
            target: { kind: 'APPLICATION', id: 13 }
        }
    ];

    vm.layout = {
        'APPLICATION/12': { x: 140, y: 10 },
        'APPLICATION/13': { x: 440, y: 50 }
    };

    vm.contextMenus = {
        node: (d) => {
            return [
                {
                    title: (d) => `Add Downsteam target to ${d.data.name}`,
                    action: function(elm, d, i) {
                        const dt = new Date();
                        const node = { kind: 'APPLICATION', id: ""+dt, name: "Settlements Service" };
                        const flow = { kind: 'LOGICAL_FLOW', id: "f/"+dt, source: d.data, target : node}
                        return [
                            { command: 'ADD_NODE', payload: node },
                            { command: 'ADD_FLOW', payload: flow }
                        ];
                    }
                }, {
                    title: (d) => `Add Upstream source from ${d.data.name}`,
                    action: function(elm, d, i) {
                        const dt = new Date();
                        const node = { kind: 'APPLICATION', id: ""+dt, name: "Payments Service" };
                        const flow = { kind: 'LOGICAL_FLOW', id: "f/"+dt, target: d.data, source : node}
                        return [
                            { command: 'ADD_NODE', payload: node },
                            { command: 'ADD_FLOW', payload: flow }
                        ];
                    }
                }
            ]
        },
        canvas: (d) => {
            return [
                {
                    title: (d) => `Add Application`,
                    action: function(elm, d, i) {
                        const dt = new Date();
                        const node = { kind: 'APPLICATION', id: ""+dt, name: "Pricing Engine" };
                        return [
                            { command: 'ADD_NODE', payload: node },
                        ];
                    }
                }
            ]
        }
    }

    vm.addNode = () => {
        const dt = new Date();
        const node = { kind: 'APPLICATION', id: ""+dt, name: "Please work" };
        const commands = [ { command: 'ADD_NODE', payload: node }];

    }
}


controller.$inject = [
    '$stateParams',
    '$timeout'
];


const view = {
    template: require('./playpen3.html'),
    controller,
    controllerAs: 'ctrl',
    bindToController: true,
    scope: {}
};


export default view;
//
//
//
//
// const nodeMenu = [
//     {
//         title: (d) => `Add upstream source to ${d.data.name}`,
//         action: function(elm, d, i) {
//             const dt = new Date();
//             state.model.nodes.push({ id: 'APPLICATION/'+dt, data: { kind: 'APPLICATION', id: 14, name: "Settlements Service" }});
//             state.model.flows.push({ id: 'LOGICAL_FLOW/'+dt, source:'APPLICATION/'+dt , target: d.id, data: { kind: 'LOGICAL_FLOW', id: 1214 }});
//             const pos = Object.assign({}, layoutFor(d));
//             console.log(event)
//             pos.x = layoutFor(d).x + event.layerX;
//             pos.y = layoutFor(d).y + event.layerY;
//             state.layout['APPLICATION/'+dt] = pos;
//             draw();
//             return 'foo'
//         }
//     }, {
//         title: (d) => `Add downstream flow from ${d.data.name}`,
//         action: function(elm, d, i) {
//             const dt = new Date();
//             state.model.nodes.push({ id: 'APPLICATION/'+dt, data: { kind: 'APPLICATION', id: 14, name: "Confirmations Engine" }});
//             state.model.flows.push({ id: 'LOGICAL_FLOW/'+dt, source: d.id, target: 'APPLICATION/'+dt, data: { kind: 'LOGICAL_FLOW', id: 1214 }});
//             const pos = Object.assign({}, layoutFor(d));
//             pos.x += 70;
//             pos.y += 70;
//             state.layout['APPLICATION/'+dt] = pos;
//             draw();
//         }
//     }
// ];
//
//
// const canvasMenu = [
//     {
//         title: 'Add Node',
//         action: function(elm, d, i) {
//             const dt = new Date();
//             state.model.nodes.push({ id: 'APPLICATION/'+dt, data: { kind: 'APPLICATION', id: 14, name: "Trade Booking System" }});
//             draw();
//         }
//     }
// ];

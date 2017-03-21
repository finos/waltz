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
import {toGraphId} from './flow-diagram-utils';


const initialState = {
    layout: {},
    nodes: [],
    flows: [],
    visibility: {
        popup: false
    },
    popup: {
        title: '',
        description: '',
    }
};

function convertFlowsToOptions(flows = [], node, isUpstream) {
    const counterpart = isUpstream
        ? 'source'
        : 'target';

    const self = isUpstream
        ? 'target'
        : 'source';

    return _
        .chain(flows)
        .filter(f => f[self].id === node.data.id)
        .map(f => Object.assign({}, f, { kind: 'LOGICAL_FLOW' }))
        .map(f => {
            return {
                entity: f[counterpart],
                commands: [
                    { command: 'ADD_NODE', payload: f[counterpart] },
                    { command: 'ADD_FLOW', payload: f }
                ]
            };
        })
        .value();
}


function mkPopupParams(node, isUpstream, options) {
    const direction = isUpstream
        ? 'upstream'
        : 'downstream';
    const popup = {
        title: `Add ${direction} node to ${node.data.name}`,
        description: `Select an ${direction} node from the list below: `,
        options
    };
    return popup;
}


function prepareAddFlowPopup(node, isUpstream = true, logicalFlowStore) {
    if (!node || !logicalFlowStore) return;

    return logicalFlowStore
        .findByEntityReference(node.data)
        .then(flows => convertFlowsToOptions(flows, node, isUpstream))
        .then(options => mkPopupParams(node, isUpstream, options ));
}


function controller($q, $state, $timeout, logicalFlowStore) {
    const vm = Object.assign(this, initialState);

    let sendCommands = null;


    vm.contextMenus = {
        node: (d) => {
            return [
                {
                    title: (d) => `Add Upstream source to ${d.data.name}`,
                    action: (elm, d, i) => {
                        $timeout(() => {
                            prepareAddFlowPopup(d, true, logicalFlowStore)
                                .then(popup => {
                                    vm.popup = popup;
                                    vm.visibility.popup = true;
                                });
                        });
                    }
                }, {
                    title: (d) => `Add Downstream target from ${d.data.name}`,
                    action: (elm, d, i) => {
                        $timeout(() => {
                            prepareAddFlowPopup(d, false, logicalFlowStore)
                                .then(popup => {
                                    vm.popup = popup;
                                    vm.visibility.popup = true;
                                });
                        });
                    }
                }, {
                    divider: true
                }, {
                    title: (d) => `Remove ${d.data.name}`,
                    action: (elm, d, i) => {
                        return [
                            { command: 'REMOVE_NODE', payload: d }
                        ];
                    }
                }, {
                    divider: true
                }, {
                    title: (d) => `Go to ${d.data.name}`,
                    action: (elm, d, i) => {
                        $state.go('main.app.view', {id: d.data.id })
                    }
                }
            ]
        },
        canvas: (d) => {
            return [
                {
                    title: (d) => `Add Application`,
                    action: function (elm, d, i) {
                        $timeout(() => vm.visibility.popup = true);
                    }
                }
            ]
        }
    };


    vm.issueCommands = (commands) => {
        sendCommands(commands);
        vm.visibility.popup = false;
    };


    vm.addNode = () => {
        const dt = new Date();
        const node1 = { kind: 'APPLICATION', id: "a" + dt, name: "Node A" };
        const node2 = { kind: 'APPLICATION', id: "b" + dt, name: "Node B" };
        const node3 = { kind: 'ACTOR', id: "c" + dt, name: "Actor C" };
        const node4 = { kind: 'APPLICATION', id: "d" + dt, name: "Node D" };
        const node5 = { kind: 'APPLICATION', id: "e" + dt, name: "Node E" };
        const node6 = { kind: 'ACTOR', id: "f" + dt, name: "Actor F" };
        const flow12 = { kind: 'LOGICAL_FLOW', id: "f1" + dt, source: node1, target: node2 };
        const flow13 = { kind: 'LOGICAL_FLOW', id: "f2" + dt, source: node1, target: node3 };
        const flow34 = { kind: 'LOGICAL_FLOW', id: "f3" + dt, source: node3, target: node4 };
        const flow46 = { kind: 'LOGICAL_FLOW', id: "f4" + dt, source: node4, target: node6 };
        const flow56 = { kind: 'LOGICAL_FLOW', id: "f5" + dt, source: node5, target: node6 };
        const commands = [
            { command: 'ADD_NODE', payload: node1 },
            { command: 'ADD_NODE', payload: node2 },
            { command: 'ADD_NODE', payload: node3 },
            { command: 'ADD_NODE', payload: node4 },
            { command: 'ADD_NODE', payload: node5 },
            { command: 'ADD_NODE', payload: node6 },
            { command: 'ADD_FLOW', payload: flow12 },
            { command: 'ADD_FLOW', payload: flow13 },
            { command: 'ADD_FLOW', payload: flow34 },
            { command: 'ADD_FLOW', payload: flow46 },
            { command: 'ADD_FLOW', payload: flow56 }
        ];
        sendCommands(commands);
        vm.visibility.popup = false;
    };

    vm.onDiagramInit = (d) => {
        sendCommands = d.processCommands;

        const birman = { kind: 'APPLICATION', id: 22253, name: "Birman"};
        const cassowary = { kind: 'APPLICATION', id: 22666, name: "Cassowary"};
        const secrets = { kind: 'PHYSICAL_SPECIFICATION', id: 25091, name: "Secrets"};
        const architect = { kind: 'ACTOR', id: 1, name: "Architect"};

        const birmanSpec = { id: 16593, kind: 'PHYSICAL_SPECIFICATION', name: 'transfer-holdings.tsv' };

        const birmanToCassowary = { kind: 'FLOW', id: 45482, source: birman, target: cassowary };
        const cassowaryToSecrets = { kind: 'FLOW', id: 1, source: cassowary, target: secrets };
        const secretsToArchitect = { kind: 'FLOW', id: 2, source: secrets, target: architect };
        const birmanToCassowaryDecoration = { kind: 'PHYSICAL_FLOW', id: 25092, logicalFlowId: 40, specification: birmanSpec };

        sendCommands([
            { command: 'ADD_NODE', payload: birman },
            { command: 'ADD_NODE', payload: cassowary },
            { command: 'ADD_NODE', payload: secrets },
            { command: 'ADD_NODE', payload: architect },
            { command: 'ADD_FLOW', payload: birmanToCassowary },
            { command: 'ADD_FLOW', payload: cassowaryToSecrets },
            { command: 'ADD_FLOW', payload: secretsToArchitect },
            { command: 'DECORATE_FLOW', payload: birmanToCassowaryDecoration }
        ]);
    };

    vm.onDismissMenu = () => {
        vm.visibility.popup = false;
    };
}


controller.$inject = [
    '$q',
    '$state',
    '$timeout',
    'LogicalFlowStore'
];


const view = {
    template: require('./playpen3.html'),
    controller,
    controllerAs: 'ctrl',
    bindToController: true,
    scope: {}
};


export default view;

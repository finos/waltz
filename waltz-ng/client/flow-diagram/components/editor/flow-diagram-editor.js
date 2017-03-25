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
import {initialiseData} from '../../../common';


/**
 * @name waltz-flow-diagram-editor
 *
 * @description
 * This component ...
 */


const bindings = {};


const initialState = {
    layout: { positions: {}, shapes: {}, subject: 'APPLICATION/22253' },
    nodes: [],
    flows: [],
    visibility: {
        addLogicalFlowPopup: false,
        addAnnotationPopup: false
    },
    popup: {
        title: '',
        description: '',
    }
};


const template = require('./flow-diagram-editor.html');


function createSampleDiagram() {
    const birman = { kind: 'APPLICATION', id: 22253, name: "Birman"};
    const bear = { kind: 'APPLICATION', id: 22276, name: "Bear"};
    const cassowary = { kind: 'APPLICATION', id: 22666, name: "Cassowary"};
    const architect = { kind: 'ACTOR', id: 1, name: "Architect with a very long name"};

    const bearToBirman = { kind: 'LOGICAL_FLOW', id: 45878, source: bear, target: birman };
    const birmanToCassowary = { kind: 'LOGICAL_FLOW', id: 45482, source: birman, target: cassowary };
    const cassowaryToArchitect = { kind: 'LOGICAL_FLOW', id: 1, source: cassowary, target: architect };

    const secrets = { kind: 'PHYSICAL_SPECIFICATION', id: 25091, name: "Secrets"};
    const transferHoldings = { id: 16593, kind: 'PHYSICAL_SPECIFICATION', name: 'transfer-holdings.tsv' };
    const transferPurchases = { id: 16594, kind: 'PHYSICAL_SPECIFICATION', name: 'transfer-purchases.tsv' };

    const birmanPhysicalFlow1 = { kind: 'PHYSICAL_FLOW', id: 25092, specification: transferHoldings };
    const birmanPhysicalFlow2 = { kind: 'PHYSICAL_FLOW', id: 25092, specification: transferPurchases };
    const cassowaryPhysicalFlow = { kind: 'PHYSICAL_FLOW', id: 25091, specification: secrets };

    const birmanToCassowaryDecoration1 = { ref: { id: 45482, kind: 'LOGICAL_FLOW' }, decoration: birmanPhysicalFlow1 };
    const birmanToCassowaryDecoration2 = { ref: { id: 45482, kind: 'LOGICAL_FLOW' }, decoration: birmanPhysicalFlow2 };
    const cassowaryToArchitectDecoration = { ref: { id: 1, kind: 'LOGICAL_FLOW' }, decoration: cassowaryPhysicalFlow };

    const bearAnnotation = {
        id: 2, ref: {id: 22276, kind: 'APPLICATION' },
        note: "However you choose to use LOOPY, hopefully it can give you not just the software tools, but also the mental tools to understand the complex systems of the world around us. It's a hot mess out there. ",
        dy: 37, dx: -62 };

    const architectAnnotation = {
        id: 1, ref: {id: 1, kind: 'ACTOR' },
        note: "An architect, probably very clever",
        dy: 37, dx: -62 };

    return [
        { command: 'ADD_NODE', payload: birman },
        { command: 'ADD_NODE', payload: cassowary },
        { command: 'ADD_NODE', payload: architect },
        { command: 'ADD_NODE', payload: bear },

        { command: 'ADD_FLOW', payload: birmanToCassowary },
        { command: 'ADD_FLOW', payload: cassowaryToArchitect },
        { command: 'ADD_FLOW', payload: bearToBirman },

        { command: 'ADD_DECORATION', payload: birmanToCassowaryDecoration1 },
        { command: 'ADD_DECORATION', payload: birmanToCassowaryDecoration2 },
        { command: 'ADD_DECORATION', payload: cassowaryToArchitectDecoration },
        { command: 'ADD_ANNOTATION', payload: bearAnnotation },
        { command: 'ADD_ANNOTATION', payload: architectAnnotation }
    ];
}


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


function prepareAddFlowPopup(node, isUpstream = true, logicalFlowStore) {
    if (!node || !logicalFlowStore) return;

    return logicalFlowStore
        .findByEntityReference(node.data)
        .then(flows => convertFlowsToOptions(flows, node, isUpstream))
        .then(options => {
            const direction = isUpstream
                ? 'upstream'
                : 'downstream';
            const popup = {
                title: `Add ${direction} node to ${node.data.name}`,
                description: `Select an ${direction} node from the list below: `,
                options
            };
            return popup;
        });
}


function prepareAddAnnotationPopup(node) {
    if (!node) return;

    return { title: `Add annotation to ${node.data.name}`, description: ''}
}


function mkNodeMenu($state, $timeout, logicalFlowStore, vm) {
    return (d) => {
        return [
            {
                title: (d) => `Add upstream source to ${d.data.name}`,
                action: (elm, d, i) => {
                    $timeout(() => {
                        prepareAddFlowPopup(d, true, logicalFlowStore)
                            .then(popup => {
                                vm.popup = popup;
                                vm.visibility.addLogicalFlowPopup = true;
                            });
                    });
                }
            }, {
                title: (d) => `Add downstream target from ${d.data.name}`,
                action: (elm, d, i) => {
                    $timeout(() => {
                        prepareAddFlowPopup(d, false, logicalFlowStore)
                            .then(popup => {
                                vm.popup = popup;
                                vm.visibility.addLogicalFlowPopup = true;
                            });
                    });
                }
            }, {
                title: (d) => `Add annotation to ${d.data.name}`,
                action: (elm, d, i) => {
                    $timeout(() => {
                        const popup = prepareAddAnnotationPopup(d)
                        vm.popup = popup;
                        vm.visibility.addAnnotationPopup = true;
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
    }
}


function mkFlowBucketMenu() {
    return (d) => {
        return [
            { title: 'hello'},
            { divider: true },
            { title: 'bucket'},
        ];
    };
}


function mkAnnotationMenu() {
    return (d) => {
        return [
            {
                title: 'Edit',
                action: (elm, d, i) => {
                    return [
                        { command: 'UPDATE_ANNOTATION', payload: { annotation: d, text: 'Hello World' } }
                    ];
                }
            },
            { divider: true },
            {
                title: 'Remove',
                action: (elm, d, i) => {
                    return [
                        { command: 'REMOVE_ANNOTATION', payload: d }
                    ];
                }
            },
        ];
    };
}


function mkCanvasMenu() {
    return (d) => {
        return [
            {
                title: (d) => `Add Some Application`,
                action: function (elm, d, i) {
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
                    return [
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
                }
            }
        ]
    };
}


function controller($q, $state, $timeout, logicalFlowStore) {
    const vm = initialiseData(this, initialState);

    let sendCommands = null;

    vm.contextMenus = {
        node: mkNodeMenu($state, $timeout, logicalFlowStore, vm),
        flowBucket: mkFlowBucketMenu(),
        annotation: mkAnnotationMenu(),
        canvas: mkCanvasMenu()
    };

    vm.issueCommands = (commands) => {
        sendCommands(commands);
        vm.onDismissPopup();
    };

    vm.onDismissPopup = () => {
        vm.visibility.addLogicalFlowPopup = false;
        vm.visibility.addAnnotationPopup = false;
    };

    vm.onDiagramInit = (d) => {
        sendCommands = d.processCommands;
        sendCommands(createSampleDiagram());
    };

}


controller.$inject = [
    '$q',
    '$state',
    '$timeout',
    'LogicalFlowStore'
];



const component = {
    template,
    bindings,
    controller
};


export default component;
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
import angular from 'angular';
import {initialiseData} from '../../../common';
import {createSampleDiagram} from '../../flow-diagram-utils';

/**
 * @name waltz-flow-diagram-editor
 *
 * @description
 * This component ...
 */


const bindings = {
    nodes: '<',
    initialModel: '<'
};


const initialState = {
    visibility: {
        logicalFlowPopup: false,
        annotationPopup: false
    },
    popup: {
        title: '',
        description: '',
    }
};


const template = require('./flow-diagram-editor.html');



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


function prepareAddFlowPopup(graphNode, isUpstream = true, logicalFlowStore) {
    if (!graphNode || !logicalFlowStore) return;

    return logicalFlowStore
        .findByEntityReference(graphNode.data)
        .then(flows => convertFlowsToOptions(flows, graphNode, isUpstream))
        .then(options => {
            const description = isUpstream
                ? 'Select an upstream node from the list below:'
                : 'Select a downstream node from the list belows:';
            const direction = isUpstream
                ? 'Upstream'
                : 'Downstream';
            const popup = {
                title: `Add ${direction} node to ${graphNode.data.name}`,
                description,
                options
            };
            return popup;
        });
}


function prepareAddAnnotationPopup(graphNode) {
    if (!graphNode) return;

    return {
        title: `Add annotation to ${graphNode.data.name}`,
        description: '',
        mkCommand: (note) => {
            return [
                {
                    command: 'ADD_ANNOTATION',
                    payload: {
                        id: +new Date(),
                        kind: 'ANNOTATION',
                        note,
                        entityReference: { id: graphNode.data.id, kind: graphNode.data.kind }
                    }
                }
            ];
        }
    }
}


function prepareUpdateAnnotationPopup(graphNode) {
    console.log('puap', graphNode)
    if (!graphNode) return;


    return {
        title: `Update annotation`,
        description: '',
        note: _.get(graphNode, 'data.note') || "",
        mkCommand: (note) => {
            return [
                {
                    command: 'UPDATE_ANNOTATION',
                    payload: {
                        id: graphNode.id,
                        note,
                    }
                }
            ];
        }
    }
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
                                vm.visibility.logicalFlowPopup = true;
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
                                vm.visibility.logicalFlowPopup = true;
                            });
                    });
                }
            }, {
                title: (d) => `Add annotation to ${d.data.name}`,
                action: (elm, d, i) => {
                    $timeout(() => {
                        const popup = prepareAddAnnotationPopup(d);
                        vm.popup = popup;
                        vm.visibility.annotationPopup = true;
                    });
                }
            }, {
                divider: true
            }, {
                title: (d) => `Remove ${d.data.name}`,
                action: (elm, d, i) =>
                    vm.issueCommands([{command: 'REMOVE_NODE', payload: d}])
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


function mkAnnotationMenu(commandProcessor, $timeout, vm) {
    return (d) => {
        return [
            {
                title: 'Edit',
                action: (elm, d, i) => {
                    $timeout(() => {
                        const popup = prepareUpdateAnnotationPopup(d);
                        vm.popup = popup;
                        vm.visibility.annotationPopup = true;
                    });


                }
            },
            { divider: true },
            {
                title: 'Remove',
                action: (elm, d, i) => {
                    commandProcessor([{ command: 'REMOVE_ANNOTATION', payload: d }]);
                }
            },
        ];
    };
}


function mkCanvasMenu(commandProcessor) {
    return (d) => {
        return [
            {
                title: (d) => `Add some applications`,
                action: function (elm, d, i) {
                    return createSampleDiagram(commandProcessor);
                }
            }
        ]
    };
}


function controller($state,
                    $timeout,
                    logicalFlowStore,
                    flowDiagramStateService) {
    const vm = initialiseData(this, initialState);

    vm.contextMenus = {
        node: mkNodeMenu($state, $timeout, logicalFlowStore, vm),
        flowBucket: mkFlowBucketMenu(flowDiagramStateService.processCommands),
        annotation: mkAnnotationMenu(flowDiagramStateService.processCommands, $timeout, vm),
        canvas: mkCanvasMenu(flowDiagramStateService.processCommands)
    };

    vm.issueCommands = (commands) => {
        flowDiagramStateService.processCommands(commands);
        vm.onDismissPopup();
    };

    vm.onDismissPopup = () => {
        vm.visibility.logicalFlowPopup = false;
        vm.visibility.annotationPopup = false;
    };

    vm.onDiagramInit = (d) => {
    };

    vm.doSave = () => {
        flowDiagramStateService.save()
            .then(r => vm.saveResp = r)
    };

    vm.$onChanges = (c) => {
        if (c.initialModel) {
            vm.workingModel = angular.copy(vm.initialModel);
        }
    };

}


controller.$inject = [
    '$state',
    '$timeout',
    'LogicalFlowStore',
    'FlowDiagramStateService'
];


const component = {
    template,
    bindings,
    controller
};


export default component;
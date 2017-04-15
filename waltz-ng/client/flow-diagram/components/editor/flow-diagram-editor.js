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
import {createSampleDiagram, toGraphId} from '../../flow-diagram-utils';

/**
 * @name waltz-flow-diagram-editor
 *
 * @description
 * This component ...
 */


const bindings = {
};


const initialState = {
    visibility: {
        logicalFlowPopup: false,
        annotationPopup: false,
        physicalFlowPopup: false
    },
    popup: {
        title: '',
        description: '',
    }
};


const template = require('./flow-diagram-editor.html');



function prepareAddLogicalFlowPopup(graphNode, isUpstream = true, logicalFlowStore, flowDiagramStateService) {
    if (!graphNode || !logicalFlowStore) return;

    return logicalFlowStore
        .findByEntityReference(graphNode.data)
        .then(flows => {
            const popup = {
                logicalFlows: flows,
                node: graphNode.data,
                existingEntities: flowDiagramStateService.getAllEntities(),
                isUpstream
            };
            return popup;
        });
}


function prepareAddAnnotationPopup(graphNode) {
    if (!graphNode) return;

    return {
        annotation: {
            entityReference: { kind: graphNode.data.kind, id: graphNode.data.id }
        }
    };
}


function prepareUpdateAnnotationPopup(graphNode) {
    if (!graphNode) return;

    return {
        annotation: graphNode.data
    };
}


function mkNodeMenu($state, $timeout, logicalFlowStore, vm, flowDiagramStateService) {
    return (d) => {
        return [
            {
                title: (d) => `Add upstream source to ${d.data.name}`,
                action: (elm, d, i) => {
                    $timeout(() => {
                        prepareAddLogicalFlowPopup(d, true, logicalFlowStore, flowDiagramStateService)
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
                        prepareAddLogicalFlowPopup(d, false, logicalFlowStore, flowDiagramStateService)
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


function preparePhysicalFlowPopup(
    $q,
    logicalFlow,
    physicalFlowStore,
    physicalSpecificationStore,
    flowDiagramStateService)
{
    const selector = {
        entityReference: { id: logicalFlow.id, kind: logicalFlow.kind },
        scope: 'EXACT'
    };

    const physFlowPromise = physicalFlowStore.findByLogicalFlowId(logicalFlow.id);
    const physSpecPromise = physicalSpecificationStore.findBySelector(selector);

    return $q
        .all([physFlowPromise, physSpecPromise])
        .then(([physicalFlows = [], physicalSpecifications = []]) => {
            const popup = {
                logicalFlow,
                physicalFlows,
                physicalSpecifications,
                existingEntities: flowDiagramStateService.getAllEntities()
            };
            return popup;
        })

}


function mkFlowBucketMenu($q, $timeout, vm, flowDiagramStateService, physicalFlowStore, physicalSpecificationStore) {
    return (d) => {
        return [
            {
                title: 'Add annotation',
                action: (elm, d, i) => {
                    $timeout(() => {
                        const popup = prepareAddAnnotationPopup(d);
                        vm.popup = popup;
                        vm.visibility.annotationPopup = true;
                    });
                }
            },
            {
                title: 'Define physical flows',
                action: (elm, logicalFlowNode, i) => {
                    $timeout(() => {
                        preparePhysicalFlowPopup(
                            $q,
                            logicalFlowNode.data,
                            physicalFlowStore,
                            physicalSpecificationStore,
                            flowDiagramStateService)
                            .then(popup => {
                                vm.popup = popup;
                                vm.visibility.physicalFlowPopup = true;
                            });
                    });

                }
            },
            { divider: true },
            {
                title: 'Remove Flow',
                action: (elm, d, i) =>
                    flowDiagramStateService.processCommands([{command: 'REMOVE_FLOW', payload: d}])
            },
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


function controller($q,
                    $state,
                    $timeout,
                    flowDiagramStateService,
                    logicalFlowStore,
                    physicalFlowStore,
                    physicalSpecificationStore,
                    notification) {
    const vm = initialiseData(this, initialState);

    vm.contextMenus = {
        node: mkNodeMenu($state, $timeout, logicalFlowStore, vm, flowDiagramStateService),
        flowBucket: mkFlowBucketMenu($q, $timeout, vm,  flowDiagramStateService, physicalFlowStore, physicalSpecificationStore),
        annotation: mkAnnotationMenu(flowDiagramStateService.processCommands, $timeout, vm),
        canvas: mkCanvasMenu(flowDiagramStateService.processCommands)
    };

    vm.issueCommands = (commands) => {
        flowDiagramStateService.processCommands(commands);
        vm.onDismissPopup();
    };

    vm.onDismissPopup = () => {
        vm.visibility.annotationPopup = false;
        vm.visibility.logicalFlowPopup = false;
        vm.visibility.physicalFlowPopup = false;
    };

    vm.onDiagramInit = (d) => {
    };

    vm.doSave = () => {
        flowDiagramStateService.save()
            .then(r => vm.saveResp = r)
            .then(() => notification.success('Saved'))
    };

    vm.$onChanges = (c) => {
        if (c.initialModel) {
            vm.workingModel = angular.copy(vm.initialModel);
        }
    };

}


controller.$inject = [
    '$q',
    '$state',
    '$timeout',
    'FlowDiagramStateService',
    'LogicalFlowStore',
    'PhysicalFlowStore',
    'PhysicalSpecificationStore',
    'Notification'
];



const component = {
    template,
    bindings,
    controller
};


export default component;
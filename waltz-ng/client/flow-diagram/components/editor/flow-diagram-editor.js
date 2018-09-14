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

import {initialiseData} from "../../../common";
import template from "./flow-diagram-editor.html";
import {CORE_API} from "../../../common/services/core-api-utils";


/**
 * @name waltz-flow-diagram-editor
 *
 * @description
 * This component ...
 */


const bindings = {
    parentEntityRef: "<",
    onCancel: "<",
    onView: "<"
};


const initialState = {
    visibility: {
        disjointNodePopup: false,
        logicalFlowPopup: false,
        annotationPopup: false,
        physicalFlowPopup: false,
        diagramInfoPopup: false
    },
    popup: {
        title: "",
        description: "",
    }
};


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


function mkNodeMenu($timeout, logicalFlowStore, vm, flowDiagramStateService) {
    return (d) => {
        return [
            {
                title: (d) => `Add upstream source to ${d.data.name}`,
                action: (elm, d) => {
                    $timeout(() => {
                        prepareAddLogicalFlowPopup(d, true, logicalFlowStore, flowDiagramStateService)
                            .then(popup => {
                                vm.popup = popup;
                                vm.visibility.logicalFlowPopup = true;
                                vm.visibility.anyPopup = true;
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
                                vm.visibility.anyPopup = true;
                            });
                    });
                }
            }, {
                title: (d) => `Add annotation to ${d.data.name}`,
                action: (elm, d, i) => {
                    $timeout(() => {
                        vm.popup = prepareAddAnnotationPopup(d);
                        vm.visibility.annotationPopup = true;
                    });
                }
            }, {
                divider: true
            }, {
                title: (d) => `Remove ${d.data.name}`,
                action: (elm, d, i) =>
                    vm.issueCommands([{command: "REMOVE_NODE", payload: d}])
            }
        ]
    }
}


function mkDisjointNodeMenu($timeout, vm, flowDiagramStateService) {
    return (d) => {
        return [
            {
                title: (d) => "Add an actor or application",
                action: (elm, d, i) => {
                    $timeout(() => {
                        const popup = {
                            existingEntities: flowDiagramStateService.getAllEntities(),
                        };
                        vm.popup = popup;
                        vm.visibility.disjointNodePopup = true;
                        vm.visibility.anyPopup = true;
                    });
                }
            },
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
        scope: "EXACT"
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

        const removeFlow = {
            title: "Remove Flow",
            action: (elm, d, i) =>
                flowDiagramStateService.processCommands([{command: "REMOVE_FLOW", payload: d}])
        };

        if(d.data.isRemoved) {
            return [removeFlow];
        } else {
            return [
                {
                    title: "Add annotation",
                    action: (elm, d, i) => {
                        $timeout(() => {
                            const popup = prepareAddAnnotationPopup(d);
                            vm.popup = popup;
                            vm.visibility.annotationPopup = true;
                            vm.visibility.anyPopup = true;
                        });
                    }
                },
                {
                    title: "Define physical flows",
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
                                    vm.visibility.anyPopup = true;
                                });
                        });

                    }
                },
                { divider: true },
                removeFlow,
            ];
        }
    };
}


function mkAnnotationMenu(commandProcessor, $timeout, vm) {
    return (d) => {
        return [
            {
                title: "Edit",
                action: (elm, d, i) => {
                    $timeout(() => {
                        const popup = prepareUpdateAnnotationPopup(d);
                        vm.popup = popup;
                        vm.visibility.annotationPopup = true;
                        vm.visibility.anyPopup = true;
                    });
                }
            },
            { divider: true },
            {
                title: "Remove",
                action: (elm, d, i) => {
                    commandProcessor([{ command: "REMOVE_ANNOTATION", payload: d }]);
                }
            },
        ];
    };
}


function controller($q,
                    $scope,
                    $timeout,
                    flowDiagramStateService,
                    logicalFlowStore,
                    notification,
                    physicalFlowStore,
                    physicalSpecificationStore,
                    preventNavigationService,
                    serviceBroker) {
    const vm = initialiseData(this, initialState);

    vm.contextMenus = {
        canvas: mkDisjointNodeMenu($timeout, vm, flowDiagramStateService),
        node: mkNodeMenu($timeout, logicalFlowStore, vm, flowDiagramStateService),
        flowBucket: mkFlowBucketMenu($q, $timeout, vm,  flowDiagramStateService, physicalFlowStore, physicalSpecificationStore),
        annotation: mkAnnotationMenu(flowDiagramStateService.processCommands, $timeout, vm),
    };

    preventNavigationService.setupWarningDialog($scope, () => flowDiagramStateService.isDirty());

    vm.issueCommands = (commands) => {
        flowDiagramStateService.processCommands(commands);
        vm.onDismissPopup();
    };

    vm.onDismissPopup = () => {
        vm.visibility.annotationPopup = false;
        vm.visibility.disjointNodePopup = false;
        vm.visibility.logicalFlowPopup = false;
        vm.visibility.physicalFlowPopup = false;
        vm.visibility.diagramInfoPopup = false;
        vm.visibility.anyPopup = false;
    };

    vm.doSave = () => {
        flowDiagramStateService.save()
            .then(r => vm.id = r)
            .then(() => notification.success("Saved"))
    };

    vm.$onChanges = (c) => {
        const state = flowDiagramStateService.getState();
        vm.title = state.model.title;
        vm.id = state.diagramId;
        vm.description = state.model.description;
    };

    vm.onOpenDiagramInfoPopup = () => {
        vm.visibility.diagramInfoPopup = true;
        vm.visibility.anyPopup = true;
    };

    vm.onSaveTitle = (id, t) => {
        flowDiagramStateService.processCommands([{
            command: "SET_TITLE",
            payload: t.newVal
        }]);
        vm.title = t.newVal;

        flowDiagramStateService.updateName()
            .then(() => notification.success("Saved Title"))
    };

    vm.onSaveDescription = (id, t) => {
        flowDiagramStateService.processCommands([{
            command: "SET_DESCRIPTION",
            payload: t.newVal
        }]);
        vm.description = t.newVal;

        flowDiagramStateService.updateDescription()
            .then(() => notification.success("Saved Description"))
    };

    vm.doRemove = () => {
        if (confirm("Are you sure you wish to delete this diagram ?")) {
            serviceBroker
                .execute(
                    CORE_API.FlowDiagramStore.deleteForId,
                    [ vm.id] )
                .then(() => {
                    flowDiagramStateService.reset();
                    vm.onCancel();
                    notification.warning("Diagram deleted");
                });
        }
    };

}


controller.$inject = [
    "$q",
    "$scope",
    "$timeout",
    "FlowDiagramStateService",
    "LogicalFlowStore",
    "Notification",
    "PhysicalFlowStore",
    "PhysicalSpecificationStore",
    "PreventNavigationService",
    "ServiceBroker"
];


const component = {
    template,
    bindings,
    controller
};


export default component;
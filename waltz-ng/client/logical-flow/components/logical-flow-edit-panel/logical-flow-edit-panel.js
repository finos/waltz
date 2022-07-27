/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017, 2018, 2019 Waltz open source project
 * See README.md for more information
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific
 *
 */

import _ from "lodash";
import {CORE_API} from "../../../common/services/core-api-utils";
import {initialiseData} from "../../../common";
import {mkTweakers} from "../source-and-target-graph/source-and-target-utilities";

import template from "./logical-flow-edit-panel.html";
import {displayError} from "../../../common/error-utils";
import {sameRef} from "../../../common/entity-utils";
import {event} from "d3-selection";
import {entity} from "../../../common/services/enums/entity";
import {loadFlowClassificationRatings} from "../../../flow-classification-rule/flow-classification-utils";
import toasts from "../../../svelte-stores/toast-store";
import DataTypeInfoPanel from "../data-type-info-panel/DataTypeInfoPanel.svelte";

const bindings = {
    parentEntityRef: "<",
};


const initialState = {
    allActors: [],
    app: null,
    appsById: {},
    dataTypeUsages: [],
    flows: [],
    isDirty: false,
    mode: "", // editCounterpart | editDataTypeUsage
    physicalFlows: [],
    selectedCounterpart: null,
    selectedDecorators: null,
    selectedFlow: null,
    selectedUsages: [],
    dataTypeInfo: null,
    DataTypeInfoPanel
};


function mkNewFlow(source, target) {
    return {
        source,
        target
    };
}


function mkAddFlowCommand(flow) {
    return {
        source: flow.source,
        target: flow.target
    };
}


function notifyIllegalFlow(notification, primaryApp, counterpartRef) {
    if (primaryApp.id === counterpartRef.id && counterpartRef.kind === "APPLICATION") {
        notification.warning("An application may not link to itself.");
        return true;
    }
    return false;
}


function vetoMove(isDirty) {
    if (isDirty) {
        alert("Unsaved changes, either apply them or cancel");
        return true;
    }
    return false;
}



function scrollIntoView(element, $window) {
    element.scrollIntoView({
        behavior: "smooth",
        block: "start",
    });
    $window.scrollBy(0, -90);
}



function filterByType(typeId, flows = [], decorators = []) {
    if (typeId === 0) {
        return {
            selectedTypeId: 0,
            decorators,
            flows
        };
    }

    const ds = _.filter(decorators, d => d.decoratorEntity.id === typeId);
    const dataFlowIds = _.map(ds, "dataFlowId");
    const fs = _.filter(flows, f => _.includes(dataFlowIds, f.id));

    return {
        filterApplied: true,
        decorators: ds,
        flows: fs
    };
}


function controller($element,
                    $q,
                    $scope,
                    $window,
                    serviceBroker) {
    const vm = initialiseData(this, initialState);

    function applyFilter() {
        $scope.$applyAsync(() => {
            const filterFunction = vm.activeFilter || resetFilter;
            vm.filteredFlowData = filterFunction();
            scrollIntoView($element[0], $window);
        });
    }

    function resetFilter() {
        return {
            filterApplied: false,
            flows: vm.logicalFlows,
            decorators: vm.logicalFlowDecorators
        };
    }

    vm.showAll = () => vm.filteredFlowData = resetFilter();


    vm.$onChanges = (changes) => {
        loadFlowClassificationRatings(serviceBroker)
            .then(r => {
                vm.flowClassificationsByCode = _.keyBy(r, d => d.code);
            });

        if(vm.parentEntityRef) {
            reload()
                .then(() => {
                    const baseTweakers = {
                        source: {onSelect: a => $scope.$applyAsync(() => selectSource(a))},
                        target: {onSelect: a => $scope.$applyAsync(() => selectTarget(a))},
                        type: {
                            onSelect: d => {
                                event.stopPropagation();
                                $scope.$applyAsync(() => selectType(d));
                                vm.activeFilter = () => filterByType(
                                    d.id,
                                    vm.logicalFlows,
                                    vm.logicalFlowDecorators);
                                applyFilter();
                            }
                        },
                        typeBlock: {
                            onSelect: () => {
                                event.stopPropagation();
                                if (vm.filteredFlowData.filterApplied) {
                                    vm.activeFilter = resetFilter;
                                    applyFilter();
                                }
                            }
                        }
                    };

                    vm.flowTweakers = mkTweakers(
                        baseTweakers,
                        vm.physicalFlows,
                        vm.logicalFlows);

                    vm.showAll();
                });

            serviceBroker
                .loadViewData(
                    CORE_API.ActorStore.findAll,
                    [])
                .then(r => vm.allActors = r.data);
        }
    };

    const addFlow = (flow) => {
        const alreadyRegistered = _.some(
            vm.logicalFlows,
            f => f.source.id === flow.source.id && f.target.id === flow.target.id);

        if (! alreadyRegistered) {
            return serviceBroker
                .execute(
                    CORE_API.LogicalFlowStore.addFlow,
                    [mkAddFlowCommand(flow)])
                .then(savedFlow => vm.logicalFlows.push(savedFlow))
                .then(reload);
        } else {
            return Promise.resolve();
        }
    };


    function loadLogicalFlows() {
        return serviceBroker
            .loadViewData(
                CORE_API.LogicalFlowStore.findByEntityReference,
                [vm.parentEntityRef],
                {force: true})
            .then(r => vm.logicalFlows = r.data);
    }


    function loadPhysicalFlows() {
        return serviceBroker
            .loadViewData(
                CORE_API.PhysicalFlowStore.findByEntityReference,
                [ vm.parentEntityRef ],
                { force: true })
            .then(r => vm.physicalFlows = r.data);
    }


    function loadLogicalFlowDecorators() {
        return serviceBroker
            .loadViewData(
                CORE_API.DataTypeDecoratorStore.findBySelector,
                [ { entityReference: vm.parentEntityRef, scope: "EXACT" },
                    entity.LOGICAL_DATA_FLOW.key ],
                { force: true })
            .then(r => vm.logicalFlowDecorators = r.data);
    }


    function loadDataTypeUsages() {
        return serviceBroker
            .loadViewData(
                CORE_API.DataTypeUsageStore.findForEntity,
                [ vm.parentEntityRef ],
                { force: true })
            .then(r => vm.dataTypeUsages = r.data);
    }


    const reload = () => {
        vm.cancel();
        return $q
            .all([
                loadLogicalFlows(),
                loadLogicalFlowDecorators(),
                loadDataTypeUsages(),
                loadPhysicalFlows()
            ])
            .then(applyFilter);
    };

    const selectSource = (selection) => {
        const flowMatchingCriteria = {
            source: { id: selection.id, kind: selection.kind },
            target: vm.parentEntityRef
        };
        selectCounterpart(selection, flowMatchingCriteria);
    };

    const selectTarget = (selection) => {
        const flowMatchingCriteria = {
            source: vm.parentEntityRef,
            target: { id: selection.id, kind: selection.kind }
        };
        selectCounterpart(selection, flowMatchingCriteria);
    };

    const selectCounterpart = (selection, matchCriteria) => {
        if (vetoMove(vm.isDirty)) { return; }
        vm.setMode("editCounterpart");
        vm.selectedCounterpart = selection;
        vm.selectedFlow = _.find(vm.logicalFlows, f =>
            sameRef(f.source, matchCriteria.source) &&
            sameRef(f.target, matchCriteria.target));
        vm.selectedDecorators = vm.selectedFlow
            ? _.filter(vm.logicalFlowDecorators, { dataFlowId: vm.selectedFlow.id })
            : [];
    };

    const selectType = (type) => {
        vm.setMode("editDataTypeUsage");
        vm.selectedDataType = type;
        vm.selectedUsages = _.chain(vm.dataTypeUsages)
            .filter({ dataTypeId: type.id })
            .map("usage")
            .value();
    };

    const updateDecorators = (command) => {
        return serviceBroker
            .execute(
                CORE_API.DataTypeDecoratorStore.save,
                [vm.parentEntityRef, command])
            .then(reload)
            .then(() => toasts.success("Data flow updated"));
    };



    // INTERACTIVE FUNCTIONS

    vm.onReload = () => {
        reload();
    };

    vm.cancel = () => {
        vm.selectedCounterpart = null;
        vm.selectedDecorators = null;
        vm.selectedFlow = null;
        vm.dataTypeInfo = null;
        vm.isDirty = false;
        vm.setMode("");
    };

    vm.updateFlow = (command) => {
        if (! command.flowId) {
            return serviceBroker
                .execute(
                    CORE_API.LogicalFlowStore.addFlow,
                    [mkAddFlowCommand(vm.selectedFlow)])
                .then(flow => Object.assign(command, { flowId: flow.id }))
                .then(updateDecorators);

        } else {
            return updateDecorators(command);
        }
    };

    vm.deleteFlow = (flow) => {
        const hasPhysicalFlow = _.some(vm.physicalFlows, { logicalFlowId: flow.id });
        if (!hasPhysicalFlow) {
            serviceBroker
                .execute(
                    CORE_API.LogicalFlowStore.removeFlow,
                    [flow.id])
                .then(reload)
                .then(() => toasts.warning("Data flow removed"))
                .catch(e => displayError("System error, please contact support", e));
        } else {
            toasts.warning("This data flow has associated physical flows, please check and remove those first");
        }
    };

    vm.saveUsages = (usages = []) => {
        const dataTypeId = vm.selectedDataType.id;
        serviceBroker
            .execute(
                CORE_API.DataTypeUsageStore.save,
                [vm.parentEntityRef, dataTypeId, usages])
            .then(() => reload())
            .then(() => toasts.success("Data usage updated"));
    };

    const addSource = (kind, entity) => {
        const counterpartRef = { id: entity.id, kind, name: entity.name };
        if (notifyIllegalFlow(toasts, vm.parentEntityRef, counterpartRef)) return;
        addFlow(mkNewFlow(counterpartRef, vm.parentEntityRef))
            .then(() => selectSource(counterpartRef));
    };

    const addTarget = (kind, entity) => {
        const counterpartRef = { id: entity.id, kind, name: entity.name };
        if (notifyIllegalFlow(toasts, vm.parentEntityRef, counterpartRef)) return;
        addFlow(mkNewFlow(vm.parentEntityRef, counterpartRef))
            .then(() => selectTarget(counterpartRef));
    };

    vm.addSourceApplication = (srcApp) => {
        addSource("APPLICATION", srcApp);
    };

    vm.addSourceActor = (actor) => {
        addSource("ACTOR", actor);
    };

    vm.addTargetApplication = (targetApp) => {
        addTarget("APPLICATION", targetApp);
    };

    vm.addTargetActor = (actor) => {
        addTarget("ACTOR", actor);
    };

    vm.setDirtyChange = (dirty) => {
        vm.isDirty = dirty;
    };

    vm.setMode = (mode) => {
        if (vetoMove(vm.isDirty)) {
            return;
        }
        vm.mode = mode;
    };

    vm.onSelectDataType = (dt) => {
        vm.dataTypeInfo = dt;
    }
}


controller.$inject = [
    "$element",
    "$q",
    "$scope",
    "$window",
    "ServiceBroker"
];


const component = {
    template,
    bindings,
    controller
};


export default {
    component,
    id: "waltzLogicalFlowEditPanel"
};

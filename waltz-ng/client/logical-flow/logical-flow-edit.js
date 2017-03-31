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

import _ from "lodash";
import {loadDataTypes, loadActors} from "./registration-utils";
import {
    loadDataFlows,
    loadLogicalFlowDecorators} from "../applications/data-load";


function vetoMove(isDirty) {
    if (isDirty) {
        alert('Unsaved changes, either apply them or cancel');
        return true;
    }
    return false;
}


function loadDataTypeUsages(dataTypeUsageStore, appId, vm) {
    dataTypeUsageStore
        .findForEntity('APPLICATION', appId)
        .then(usages => vm.dataTypeUsages = usages);
}


function notifyIllegalFlow(notification, primaryApp, counterpartRef) {
    if (primaryApp.id === counterpartRef.id && counterpartRef.kind === 'APPLICATION') {
        notification.warning("An application may not link to itself.");
        return true;
    }
    return false;
}


const initialState = {
    app: null,
    appsById: {},
    dataTypes: [],
    dataTypeUsages: [],
    flows: [],
    isDirty: false,
    mode: '', // editCounterpart | editDataTypeUsage
    selectedCounterpart: null,
    selectedDecorators: null,
    selectedFlow: null,
    selectedUsages: []
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


function controller($scope,
                    application,
                    actorStore,
                    dataTypeService,
                    dataTypeUsageStore,
                    logicalFlowDecoratorStore,
                    logicalFlowStore,
                    notification) {
    const primaryAppId = application.id;

    const vm = _.defaultsDeep(this, initialState);
    vm.app = application;

    vm.primaryRef = {
        kind: 'APPLICATION',
        id: application.id,
        name: application.name
    };

    const addFlow = (flows, flow) => {
        const alreadyRegistered = _.some(
            flows,
            f => f.source.id === flow.source.id && f.target.id === flow.targetId);

        if (! alreadyRegistered) {
            return logicalFlowStore.addFlow(mkAddFlowCommand(flow))
                .then(savedFlow => flows.push(savedFlow))
                .then(reload);
        } else {
            return Promise.resolve();
        }
    };

    const reload = () => {
        loadDataFlows(logicalFlowStore, primaryAppId, vm);
        loadLogicalFlowDecorators(logicalFlowDecoratorStore, primaryAppId, vm);
        loadDataTypeUsages(dataTypeUsageStore, primaryAppId, vm);
        vm.cancel();
    };

    const selectSource = (selection) => {
        selectCounterpart(selection, { source: { id: selection.id }});
    };

    const selectTarget = (selection) => {
        selectCounterpart(selection, { target: { id: selection.id }});
    };

    const selectCounterpart = (selection, flowSelectionPredicate) => {
        if (vetoMove(vm.isDirty)) { return; }
        vm.setMode('editCounterpart');
        vm.selectedCounterpart = selection;
        vm.selectedFlow = _.find(vm.flows, flowSelectionPredicate);
        vm.selectedDecorators = vm.selectedFlow
            ? _.filter(vm.dataFlowDecorators, { dataFlowId: vm.selectedFlow.id })
            : [];
    };

    const selectType = (type) => {
        vm.setMode('editDataTypeUsage');
        vm.selectedDataType = type;
        vm.selectedUsages = _.chain(vm.dataTypeUsages)
            .filter({ dataTypeCode: type.code })
            .map('usage')
            .value();
    };

    const updateDecorators = (command) => {
        return logicalFlowDecoratorStore
            .updateDecorators(command)
            .then(reload)
            .then(() => notification.success('Data flow updated'));
    };

    vm.flowTweakers = {
        source: { onSelect: a => $scope.$applyAsync(() => selectSource(a)) },
        target: { onSelect: a => $scope.$applyAsync(() => selectTarget(a)) },
        type: { onSelect: a => $scope.$applyAsync(() => selectType(a)) }
    };

    vm.cancel = () => {
        vm.selectedCounterpart = null;
        vm.selectedDecorators = null;
        vm.selectedFlow = null;
        vm.isDirty = false;
        vm.setMode('');
    };

    vm.updateFlow = (command) => {
        if (! command.flowId) {
            return logicalFlowStore.addFlow(mkAddFlowCommand(vm.selectedFlow))
                .then(flow => Object.assign(command, { flowId: flow.id }))
                .then(updateDecorators);

        } else {
            return updateDecorators(command);
        }
    };

    vm.deleteFlow = (flow) => {
        logicalFlowStore
            .removeFlow(flow.id)
            .then(reload)
            .then(() => notification.warning('Data flow removed'))
            .catch(e => notification.error(_.split(e.data.message, '/')[0] || "System error, please contact support"));
    };

    vm.saveUsages = (usages = []) => {
        const dataTypeCode = vm.selectedDataType.code;
        dataTypeUsageStore
            .save(vm.primaryRef, dataTypeCode, usages)
            .then(() => reload())
            .then(() => notification.success('Data usage updated'));
    };

    const addSource = (kind, entity) => {
        const counterpartRef = { id: entity.id, kind, name: entity.name };
        if (notifyIllegalFlow(notification, vm.primaryRef, counterpartRef)) return;
        addFlow(vm.flows, mkNewFlow(counterpartRef, vm.primaryRef))
            .then(() => selectSource(entity));
    };

    const addTarget = (kind, entity) => {
        const counterpartRef = { id: entity.id, kind, name: entity.name };
        if (notifyIllegalFlow(notification, vm.primaryRef, counterpartRef)) return;
        addFlow(vm.flows, mkNewFlow(vm.primaryRef, counterpartRef))
            .then(() => selectTarget(entity));
    };

    vm.addSourceApplication = (srcApp) => {
        addSource('APPLICATION', srcApp);
    };

    vm.addSourceActor = (actor) => {
        addSource('ACTOR', actor);
    };

    vm.addTargetApplication = (targetApp) => {
        addTarget('APPLICATION', targetApp);
    };

    vm.addTargetActor = (actor) => {
        addTarget('ACTOR', actor);
    };

    vm.setDirtyChange = (dirty) => vm.isDirty = dirty;

    vm.setMode = (mode) => {
        if (vetoMove(vm.isDirty)) {
            return;
        }
        vm.mode = mode;
    };

    // -- BOOT
    loadDataTypes(dataTypeService, vm);
    loadActors(actorStore, vm);
    reload();

}


controller.$inject = [
    '$scope',
    'application',
    'ActorStore',
    'DataTypeService',
    'DataTypeUsageStore',
    'LogicalFlowDecoratorStore',
    'LogicalFlowStore',
    'Notification'
];


export default {
    template: require('./logical-flow-edit.html'),
    controller,
    controllerAs: 'ctrl'
};

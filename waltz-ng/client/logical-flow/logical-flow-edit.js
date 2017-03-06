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
import {
    loadDataTypes,
    loadAppAuthSources} from "./registration-utils";
import {
    loadDataFlows,
    loadDataFlowDecorators} from "../applications/data-load";


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


function notifyIllegalFlow(notification, targetApp, flowApp) {
    if (targetApp.id === flowApp.id) {
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
    selectedApp: null,
    selectedDecorators: null,
    selectedFlow: null,
    selectedUsages: []
};


function mkAppRef(app) {
    return {
        id: app.id,
        name: app.name,
        kind: 'APPLICATION'
    };
}


function mkNewFlow(source, target) {
    return {
        source: mkAppRef(source),
        target: mkAppRef(target)
    };
}


function addFlow(flows, flow) {
    const alreadyRegistered = _.some(
        flows,
        f => f.source.id === flow.source.id && f.target.id === flow.targetId);

    if (! alreadyRegistered) {
        flows.push(flow);
    }
}


function mkAddFlowCommand(flow) {
    return {
        source: flow.source,
        target: flow.target
    };
}


function controller($scope,
                    application,
                    authSourceStore,
                    dataTypeService,
                    dataTypeUsageStore,
                    logicalFlowDecoratorStore,
                    logicalFlowStore,
                    notification) {
    const primaryAppId = application.id;

    const vm = _.defaultsDeep(this, initialState);
    vm.app = application;

    vm.entityRef = {
        kind: 'APPLICATION',
        id: application.id,
        name: application.name
    };

    const reload = () => {
        loadDataFlows(logicalFlowStore, primaryAppId, vm);
        loadDataFlowDecorators(logicalFlowDecoratorStore, primaryAppId, vm);
        loadDataTypeUsages(dataTypeUsageStore, primaryAppId, vm);
        vm.cancel();
    };

    const selectSourceApp = (selection) => {
        selectApp(selection, { source: { id: selection.id }});
    };

    const selectTargetApp = (selection) => {
        selectApp(selection, { target: { id: selection.id }});
    };

    const selectApp = (selection, flowSelectionPredicate) => {
        if (vetoMove(vm.isDirty)) { return; }
        vm.setMode('editCounterpart');
        vm.selectedApp = selection;
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
            .value()
    };

    const updateDecorators = (command) => {
        return logicalFlowDecoratorStore
            .updateDecorators(command)
            .then(reload)
            .then(() => notification.success('Data flow updated'));
    };

    vm.flowTweakers = {
        source: { onSelect: a => $scope.$applyAsync(() => selectSourceApp(a)) },
        target: { onSelect: a => $scope.$applyAsync(() => selectTargetApp(a)) },
        type: { onSelect: a => $scope.$applyAsync(() => selectType(a)) }
    };

    vm.cancel = () => {
        vm.selectedApp = null;
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
            .catch(e => notification.error(_.split(e.data.message, '/')[0]));
    };

    vm.saveUsages = (usages = []) => {
        const dataTypeCode = vm.selectedDataType.code;
        dataTypeUsageStore
            .save(vm.entityRef, dataTypeCode, usages)
            .then(() => reload())
            .then(() => notification.success('Data usage updated'));
    };

    vm.addSource = (srcApp) => {
        if (notifyIllegalFlow(notification, application, srcApp)) return;
        addFlow(vm.flows, mkNewFlow(srcApp, application));
        selectSourceApp(srcApp);
    };

    vm.addTarget = (targetApp) => {
        if (notifyIllegalFlow(notification, application, targetApp)) return;
        addFlow(vm.flows, mkNewFlow(application, targetApp));
        selectTargetApp(targetApp);
    };

    vm.setDirtyChange = (dirty) => vm.isDirty = dirty;

    vm.setMode = (mode) => {
        if (vetoMove(vm.isDirty)) {
            return;
        }
        vm.mode = mode;
    };

    // -- BOOT
    loadAppAuthSources(authSourceStore, primaryAppId, vm);
    loadDataTypes(dataTypeService, vm);
    reload();

}


controller.$inject = [
    '$scope',
    'application',
    'AuthSourcesStore',
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

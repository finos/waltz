/*
 *  Waltz
 * Copyright (c) David Watkins. All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 *
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


function controller($scope,
                    application,
                    authSourceStore,
                    dataFlowStore,
                    dataFlowDecoratorStore,
                    dataTypeService,
                    dataTypeUsageStore,
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
        loadDataFlows(dataFlowStore, primaryAppId, vm);
        loadDataFlowDecorators(dataFlowDecoratorStore, primaryAppId, vm);
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
        return dataFlowDecoratorStore
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
            return dataFlowStore.addFlow(vm.selectedFlow)
                .then(flow => Object.assign(command, { flowId: flow.id }))
                .then(updateDecorators);

        } else {
            return updateDecorators(command);
        }
    };

    vm.deleteFlow = (flow) => {
        dataFlowStore
            .removeFlow(flow.id)
            .then(reload)
            .then(() => notification.warning('Data flow removed'));
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
    'DataFlowDataStore',
    'DataFlowDecoratorStore',
    'DataTypeService',
    'DataTypeUsageStore',
    'Notification'
];


export default {
    template: require('./data-flow-edit.html'),
    controller,
    controllerAs: 'ctrl'
};

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


function calculateWorkingTypes(flows = [], predicate) {
    return _.chain(flows)
        .filter(predicate)
        .map('dataType')
        .value();
}


function vetoMove(isDirty) {
    if (isDirty) {
        alert('Unsaved changes, either apply them or cancel');
        return true;
    }
    return false;
}


function addApplication(app, appGroup) {
    if (!app) return;
    if (_.some(appGroup, a => a.id === app.id)) return;
    appGroup.push(app);
}


function loadDataTypeUsages(dataTypeUsageStore, appId, vm) {
    dataTypeUsageStore
        .findForEntity('APPLICATION', appId)
        .then(usages => vm.dataTypeUsages = usages);
}


function notifyIllegalFlow(notification, targetApp, flowApp) {
    if(targetApp.id == flowApp.id) {
        notification.warning("An application may not link to itself.");
        return true;
    }
    return false;
}


const initialState = {
    app: null,
    currentDataTypes: [],
    dataTypes: [],
    dataTypeUsages: [],
    flows: [],
    graphOptions: {
        data: {
            incoming: [],
            outgoing: [],
            sources: [],
            targets: [],
            types: []
        },
        tweakers: {}
    },
    isDirty: false,
    mode: '',
    selectedApp: null,
    selectedDirection: null,
    selectedUsages: []
};


function controller($q,
                    $scope,
                    $state,
                    application,
                    authSourceStore,
                    dataFlowStore,
                    dataFlowDecoratorStore,
                    dataTypeService,
                    dataTypeUsageStore,
                    displayNameService,
                    notification) {

    const primaryAppId = application.id;
    const ouId = application.organisationalUnitId;

    const vm = _.defaultsDeep(this, initialState);
    vm.app = application;

    const reload = () => {
        loadDataFlows(dataFlowStore, primaryAppId, vm);
        loadDataFlowDecorators(dataFlowDecoratorStore, primaryAppId, vm);
        loadDataTypeUsages(dataTypeUsageStore, primaryAppId, vm);
        vm.cancel();
    };


    const selectSource = (app) => {
        if (!app) return;
        if (vetoMove(vm.isDirty)) {
            return;
        }

        vm.selectedApp = app;
        vm.selectedDirection = 'source';

        vm.currentDataTypes = calculateWorkingTypes(
            vm.flows,
            { source: { id: app.id }});

        vm.setMode('editCounterpart');

    };

    const selectTarget = (app) => {
        if (vetoMove(vm.isDirty)) {
            return;
        }
        vm.selectedApp = app;
        vm.selectedDirection = 'target';

        vm.currentDataTypes = calculateWorkingTypes(
            vm.flows,
            { target: { id: app.id }});

        vm.setMode('editCounterpart');

    };

    const selectType = (type) => {
        vm.setMode('editDataTypeUsage');
        vm.selectedDataType = type;
        vm.selectedUsages = _.chain(vm.dataTypeUsages)
            .filter({ dataTypeCode: type.code })
            .map('usage')
            .value()
    };

    const promises = [
        loadDataFlows(dataFlowStore, primaryAppId, vm),
        loadDataFlowDecorators(dataFlowDecoratorStore, primaryAppId, vm),
        loadAppAuthSources(authSourceStore, primaryAppId, vm),
        loadDataTypes(dataTypeService, vm),
        loadDataTypeUsages(dataTypeUsageStore, primaryAppId, vm)
    ];

    vm.cancel = () => {
        vm.selectedApp = null;
        vm.selectedDirection = null;
        vm.isDirty = false;
        vm.setMode('');
    };

    vm.saveFlows = (command) => {
        dataFlowStore.create(command)
            .then(() => reload())
            .then(() => notification.success('Logical flows updated'));
    };

    vm.saveUsages = (usages = []) => {
        const ref = { id: vm.app.id, kind: 'APPLICATION' };
        const dataTypeCode = vm.selectedDataType.code;
        dataTypeUsageStore.save(ref, dataTypeCode, usages)
            .then(() => reload())
            .then(() => notification.success('Data usage updated'));
    };

    vm.addSource = (app) => {
        if(notifyIllegalFlow(notification, vm.app, app)) return;
        selectSource(app);
        addApplication(app, vm.graphOptions.data.sources);
    };

    vm.addTarget = (app) => {
        if(notifyIllegalFlow(notification, vm.app, app)) return;
        selectTarget(app);
        addApplication(app, vm.graphOptions.data.targets);
    };

    vm.setDirtyChange = (dirty) => vm.isDirty = dirty;

    vm.setMode = (mode) => {
        if (vetoMove(vm.isDirty)) {
            return;
        }
        vm.mode = mode;
    };
}


controller.$inject = [
    '$q',
    '$scope',
    '$state',
    'application',
    'AuthSourcesStore',
    'DataFlowDataStore',
    'DataFlowDecoratorStore',
    'DataTypeService',
    'DataTypeUsageStore',
    'WaltzDisplayNameService',
    'Notification'
];


export default {
    template: require('./data-flow-edit.html'),
    controller,
    controllerAs: 'ctrl'
};

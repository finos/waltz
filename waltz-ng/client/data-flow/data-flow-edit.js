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
import {loadDataFlows, loadDataTypes, loadAppAuthSources, loadOrgUnitAuthSources} from "./registration-utils";
import {prepareSlopeGraph} from "./directives/slope-graph/slope-graph-utils";


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
                    dataTypeStore,
                    dataTypeUsageStore,
                    displayNameService,
                    notification) {

    const primaryAppId = application.id;
    const ouId = application.organisationalUnitId;

    const vm = _.defaultsDeep(this, initialState);
    vm.app = application;

    const reload = () => {
        loadDataFlows(dataFlowStore, primaryAppId, vm)
            .then(() => prepareData());

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
        loadAppAuthSources(authSourceStore, primaryAppId, vm),
        loadOrgUnitAuthSources(authSourceStore, ouId, vm),
        loadDataTypes(dataTypeStore, vm),
        loadDataTypeUsages(dataTypeUsageStore, primaryAppId, vm)
    ];


    const prepareData = () => {
        const dataTypes = _.chain(vm.flows)
            .map('dataType')
            .uniq()
            .value();

        const graphOptions = prepareSlopeGraph(
            primaryAppId,
            vm.flows,
            dataTypes,
            vm.appAuthSources,
            vm.ouAuthSources,
            displayNameService,
            $state);

        graphOptions.tweakers.target = {
            enter: selection => selection.on('click', app => $scope.$evalAsync(() => selectTarget(app)))
        };

        graphOptions.tweakers.source = {
            enter: selection => selection.on('click', app => $scope.$evalAsync(() => selectSource(app)))
        };

        graphOptions.tweakers.type = {
            enter: selection => selection
                .classed('clickable', true)
                .on('click', type => $scope.$evalAsync(() => selectType(type)))
        };

        vm.graphOptions = graphOptions;
    };

    $q.all(promises)
        .then(() => prepareData());

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
        selectSource(app);
        addApplication(app, vm.graphOptions.data.sources);
    };

    vm.addTarget = (app) => {
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
    'DataTypesDataService',
    'DataTypeUsageStore',
    'WaltzDisplayNameService',
    'Notification'
];


export default {
    template: require('./data-flow-edit.html'),
    controller,
    controllerAs: 'ctrl'
};

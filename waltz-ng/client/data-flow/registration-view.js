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
import angular from "angular";
import {toRef, loadDataFlows, loadDataTypes, loadAppAuthSources, loadOrgUnitAuthSources} from "./registration-utils";
import {prepareSlopeGraph} from "./directives/slope-graph/slope-graph-utils";


function controller(application,
                    dataFlowStore,
                    dataTypeStore,
                    authSourceStore,
                    displayNameService,
                    notification,
                    $state,
                    $scope,
                    $q) {

    const appId = application.id;
    const ouId = application.organisationalUnitId;

    const vm = this;

    const model = {
        newTarget: {},
        newSource: {},
        selectedApp: null,
        selectedDirection: null,
        selectedTypes: []
    };


    const isDirty = () => {
        return _.any(model.selectedTypes, st => {
            return st.original !== st.selected;
        });
    };

    const setupSelectedTypes = (selectedTypes) => {
        const all = angular.copy(vm.dataTypes);
        vm.model.selectedTypes = _.map(all, type => {
            return {
                ...type,
                selected: _.contains(selectedTypes, type.code),
                original: _.contains(selectedTypes, type.code)
            };
        });
    };

    const selectSource = (app) => {
        if (!app) return;
        if (isDirty()) {
            alert('Unsaved changes, either apply them or cancel');
            return;
        }
        model.selectedApp = app;
        model.selectedDirection = 'source';

        const selectedTypes = _.chain(vm.flows)
            .where({ source: { id: app.id }})
            .map('dataType')
            .value();

        setupSelectedTypes(selectedTypes);
    };

    const selectTarget = (app) => {
        if (isDirty()) {
            alert('Unsaved changes, either apply them or cancel');
            return;
        }
        model.selectedApp = app;
        model.selectedDirection = 'target';

        const selectedTypes = _.chain(vm.flows)
            .where({ target: { id: app.id }})
            .map('dataType')
            .value();

        setupSelectedTypes(selectedTypes);
    };


    const promises = [
        loadDataFlows(dataFlowStore, appId, vm),
        loadAppAuthSources(authSourceStore, appId, vm),
        loadOrgUnitAuthSources(authSourceStore, ouId, vm),
        loadDataTypes(dataTypeStore, vm)
    ];


    const prepareData = () => {
        const dataTypes = _.chain(vm.flows)
            .map('dataType')
            .uniq()
            .value();

        const graphData = prepareSlopeGraph(
            appId,
            vm.flows,
            dataTypes,
            vm.appAuthSources,
            vm.ouAuthSources,
            displayNameService,
            $state);

        graphData.tweakers.target = {
            enter: selection => selection.on('click', app => $scope.$evalAsync(() => selectTarget(app)))
        };

        graphData.tweakers.source = {
            enter: selection => selection.on('click', app => $scope.$evalAsync(() => selectSource(app)))
        };

        Object.assign(vm, graphData);
    };

    $q.all(promises)
        .then(() => prepareData());


    vm.cancel = () => {
        _.each(vm.model.selectedTypes, st => {
            st.selected = st.original;
        });
        model.selectedApp = null;
        model.selectedDirection = null;
    };


    vm.addSource = (sourceApp) => {
        vm.showAddUpstream = false;
        selectSource(sourceApp);
        if (!sourceApp) return;
        if (_.any(vm.data.sources, a => a.id === sourceApp.id)) return;
        vm.data.sources.push(sourceApp);
    };


    vm.addTarget = (targetApp) => {
        vm.showAddDownstream = false;
        selectTarget(targetApp);
        if (!targetApp) return;
        if (_.any(vm.data.targets, a => a.id === targetApp.id)) return;
        vm.data.targets.push(targetApp);
    };

    const reload = () => {
        loadDataFlows(dataFlowStore, appId, vm).then(() => prepareData());
        vm.cancel();
    };

    vm.apply = () => {
        const source = toRef(model.selectedDirection === 'target' ? application : model.selectedApp);
        const target = toRef(model.selectedDirection === 'source' ? application : model.selectedApp);

        const [added, removed] = _.chain(model.selectedTypes)
            .filter(st => st.selected !== st.original)
            .partition('selected')
            .value();

        const command = {
            source,
            target,
            addedTypes: _.map(added, 'code'),
            removedTypes: _.map(removed, 'code')
        };

        dataFlowStore.create(command)
            .then(() => reload())
            .then(() => notification.success('Logical flows updated'));
    };


    const addUpstreamFlow = (type, source) => {
        if (!_.any(vm.incoming, f => f.source === source.id && f.type === type.code)) {
            vm.incoming.push({ source: source.id, target: appId, type: type.code});
        }
        if (!_.any(vm.flowTypes, t => t.code === type.code)) {
            vm.flowTypes.push(type);
        }
    };

    const addDownstreamFlow = (type, target) => {
        if (!_.any(vm.outgoing, f => f.target === target.id && f.type === type.code)) {
            vm.outgoing.push({ target: target.id, source: appId, type: type.code});
        }
        if (!_.any(vm.flowTypes, t => t.code === type.code)) {
            vm.flowTypes.push(type);
        }
    };

    vm.typeSelected = (type) => {
        model.pristine = false;
        if (type.selected) {
            if (model.selectedDirection === 'source') {
                addUpstreamFlow(type, model.selectedApp);
            } else {
                addDownstreamFlow(type, model.selectedApp);
            }
        }
    };

    vm.model = model;
    vm.app = application;
    vm.isDirty = isDirty;
}

controller.$inject = [
    'application',
    'DataFlowDataStore',
    'DataTypesDataService',
    'AuthSourcesStore',
    'WaltzDisplayNameService',
    'Notification',
    '$state',
    '$scope',
    '$q'
];


export default {
    template: require('./registration-view.html'),
    controller,
    controllerAs: 'ctrl'
};

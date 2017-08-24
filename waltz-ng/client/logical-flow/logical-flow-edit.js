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
import { mkTweakers } from './components/source-and-target-graph/source-and-target-utilities';
import { CORE_API } from '../common/services/core-api-utils';
import { toEntityRef } from '../common/entity-utils';
import template from './logical-flow-edit.html';

function vetoMove(isDirty) {
    if (isDirty) {
        alert('Unsaved changes, either apply them or cancel');
        return true;
    }
    return false;
}


function notifyIllegalFlow(notification, primaryApp, counterpartRef) {
    if (primaryApp.id === counterpartRef.id && counterpartRef.kind === 'APPLICATION') {
        notification.warning("An application may not link to itself.");
        return true;
    }
    return false;
}


const initialState = {
    allActors: [],
    app: null,
    appsById: {},
    dataTypeUsages: [],
    flows: [],
    isDirty: false,
    mode: '', // editCounterpart | editDataTypeUsage
    physicalFlows: [],
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


function controller($q,
                    $scope,
                    $stateParams,
                    serviceBroker,
                    notification) {
    const vm = _.defaultsDeep(this, initialState);

    vm.$onInit = () => {
        const primaryEntityFetchMethod = $stateParams.kind === 'APPLICATION'
            ? CORE_API.ApplicationStore.getById
            : CORE_API.ActorStore.getById;

        serviceBroker
            .loadViewData(
                primaryEntityFetchMethod,
                [$stateParams.id])
            .then(r => {
                vm.primaryEntity = r.data;
                vm.primaryRef = toEntityRef(vm.primaryEntity, $stateParams.kind);
                reload()
                    .then(() => {
                        const baseTweakers = {
                            source: { onSelect: a => $scope.$applyAsync(() => selectSource(a)) },
                            target: { onSelect: a => $scope.$applyAsync(() => selectTarget(a)) },
                            type: { onSelect: a => $scope.$applyAsync(() => selectType(a)) }
                        };

                        vm.flowTweakers = mkTweakers(
                            baseTweakers,
                            vm.physicalFlows,
                            vm.flows);
                    });
            });

        serviceBroker
            .loadViewData(
                CORE_API.ActorStore.findAll,
                [])
            .then(r => vm.allActors = r.data);
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
                [vm.primaryRef],
                {force: true})
            .then(r => vm.logicalFlows = r.data);
    }

    function loadPhysicalFlows() {
        return serviceBroker
            .loadViewData(
                CORE_API.PhysicalFlowStore.findByEntityReference,
                [ vm.primaryRef ],
                { force: true })
            .then(r => vm.physicalFlows = r.data);
    }


    function loadLogicalFlowDecorators() {
        return serviceBroker
            .loadViewData(
                CORE_API.LogicalFlowDecoratorStore.findBySelectorAndKind,
                [ { entityReference: vm.primaryRef, scope: 'EXACT' }, 'DATA_TYPE' ],
                { force: true })
            .then(r => vm.logicalFlowDecorators = r.data);
    }


    function loadDataTypeUsages() {
        return serviceBroker
            .loadViewData(
                CORE_API.DataTypeUsageStore.findForEntity,
                [ vm.primaryRef ],
                { force: true })
            .then(r => vm.dataTypeUsages = r.data);
    }


    const reload = () => {
        vm.cancel();
        return $q.all([
            loadLogicalFlows(),
            loadLogicalFlowDecorators(),
            loadDataTypeUsages(),
            loadPhysicalFlows()
        ]);
    };

    const selectSource = (selection) => {
        selectCounterpart(selection, { source: { id: selection.id, kind: selection.kind }});
    };

    const selectTarget = (selection) => {
        selectCounterpart(selection, { target: { id: selection.id, kind: selection.kind }});
    };

    const selectCounterpart = (selection, flowSelectionPredicate) => {
        if (vetoMove(vm.isDirty)) { return; }
        vm.setMode('editCounterpart');
        vm.selectedCounterpart = selection;
        vm.selectedFlow = _.find(vm.logicalFlows, flowSelectionPredicate);
        vm.selectedDecorators = vm.selectedFlow
            ? _.filter(vm.logicalFlowDecorators, { dataFlowId: vm.selectedFlow.id })
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
        return serviceBroker
            .execute(
                CORE_API.LogicalFlowDecoratorStore.updateDecorators,
                [command])
            .then(reload)
            .then(() => notification.success('Data flow updated'));
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
                .then(() => notification.warning('Data flow removed'))
                .catch(e => notification.error(_.split(e.data.message, '/')[0] || "System error, please contact support"));
        } else {
            notification.warning(`This data flow has associated physical flows, please check and remove those first`)
        }
    };

    vm.saveUsages = (usages = []) => {
        const dataTypeCode = vm.selectedDataType.code;
        serviceBroker
            .execute(
                CORE_API.DataTypeUsageStore.save,
                [vm.primaryRef, dataTypeCode, usages])
            .then(() => reload())
            .then(() => notification.success('Data usage updated'));
    };

    const addSource = (kind, entity) => {
        const counterpartRef = { id: entity.id, kind, name: entity.name };
        if (notifyIllegalFlow(notification, vm.primaryRef, counterpartRef)) return;
        addFlow(mkNewFlow(counterpartRef, vm.primaryRef))
            .then(() => selectSource(counterpartRef));
    };

    const addTarget = (kind, entity) => {
        const counterpartRef = { id: entity.id, kind, name: entity.name };
        if (notifyIllegalFlow(notification, vm.primaryRef, counterpartRef)) return;
        addFlow(mkNewFlow(vm.primaryRef, counterpartRef))
            .then(() => selectTarget(counterpartRef));
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
}


controller.$inject = [
    '$q',
    '$scope',
    '$stateParams',
    'ServiceBroker',
    'Notification'
];


export default {
    template,
    controller,
    controllerAs: 'ctrl'
};

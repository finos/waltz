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

import _ from 'lodash';

import {CORE_API} from '../../../common/services/core-api-utils';
import {initialiseData} from '../../../common';

import template from './data-flow-section.html';


const bindings = {
    parentEntityRef: '<',
};


const initialState = {
    dataTypeUsages: [],
    logicalFlows: [],
    logicalFlowDecorators: [],
    physicalFlows: [],
    physicalSpecifications: [],
    visibility: {
        dataTab: 0,
        logicalFlows: false, // this is the source data ratings panel, rename
        editor: {
            logicalFlows: false,
            bulkLogicalFlows: false,
            bulkPhysicalFlows: false
        }

    }
};


function controller(serviceBroker) {
    const vm = initialiseData(this, initialState);

    function loadAdditionalAuthSourceData() {

        const orgUnitIds = _
            .chain(vm.authSources)
            .map('parentReference')
            .filter({ kind: 'ORG_UNIT'})
            .map("id")
            .uniq()
            .value();

        serviceBroker
            .loadAppData(
                CORE_API.OrgUnitStore.findByIds,
                [orgUnitIds])
            .then(r => {
                vm.orgUnits= r.data;
                vm.orgUnitsById = _.keyBy(r.data, 'id');
            });
    }


    function loadData() {
        const selector = {
            entityReference: vm.parentEntityRef,
            scope: 'EXACT'
        };

        serviceBroker
            .loadViewData(
                CORE_API.LogicalFlowStore.findByEntityReference,
                [vm.parentEntityRef])
            .then(r => vm.logicalFlows = r.data);

        serviceBroker
            .loadViewData(
                CORE_API.DataTypeUsageStore.findForEntity,
                [vm.parentEntityRef])
            .then(r => vm.dataTypeUsages = r.data);

        serviceBroker
            .loadViewData(
                CORE_API.PhysicalFlowStore.findByEntityReference,
                [vm.parentEntityRef])
            .then(r => vm.physicalFlows = r.data);

        serviceBroker
            .loadViewData(
                CORE_API.PhysicalSpecificationStore.findByEntityReference,
                [vm.parentEntityRef])
            .then(r => vm.physicalSpecifications = r.data);


        serviceBroker
            .loadViewData(
                CORE_API.LogicalFlowDecoratorStore.findBySelectorAndKind,
                [selector, 'DATA_TYPE'])
            .then(r => vm.logicalFlowDecorators = r.data);

        serviceBroker
            .loadViewData(
                CORE_API.AuthSourcesStore.findByApp,
                [ vm.parentEntityRef.id ])
            .then(r => {
                vm.authSources = r.data;
            });
    }


    vm.$onInit = () => {
        loadData();
    };

    vm.showTab = (idx) => {
        vm.visibility.dataTab = idx;
        if (idx === 2) {
            loadAdditionalAuthSourceData();
        }
    };

    vm.onPhysicalFlowsInitialise = (e) => {
        vm.physicalFlowProducesExportFn = e.exportProducesFn;
        vm.physicalFlowConsumesExportFn = e.exportConsumesFn;
        vm.physicalFlowUnusedSpecificationsExportFn = e.exportUnusedSpecificationsFn;
    };

    vm.exportPhysicalFlowProduces = () => {
        vm.physicalFlowProducesExportFn();
    };

    vm.exportPhysicalFlowConsumes = () => {
        vm.physicalFlowConsumesExportFn();
    };

    vm.exportPhysicalFlowUnusedSpecifications = () => {
        vm.physicalFlowUnusedSpecificationsExportFn();
    };

    vm.isAnyEditorVisible = () => {
        return _.some(vm.visibility.editor, r => r);
    };

    vm.resetToViewMode = () => {
        vm.visibility.editor = Object.assign({}, initialState.visibility.editor);
        loadData();
    };

}


controller.$inject = [
    'ServiceBroker'
];


const component = {
    template,
    bindings,
    controller
};


export default {
    component,
    id: 'waltzDataFlowSection'
};

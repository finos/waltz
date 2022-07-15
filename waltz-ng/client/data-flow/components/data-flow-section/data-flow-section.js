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
import {mkSelectionOptions} from "../../../common/selector-utils";

import template from "./data-flow-section.html";
import {entity} from "../../../common/services/enums/entity";
import FlowClassificationLegend
    from "../../../flow-classification-rule/components/svelte/FlowClassificationLegend.svelte";
import LogicalFlowScrollPanel from "../svelte/FlowDecoratorExplorerPanel.svelte"
import {lastViewedFlowTabKey} from "../../../user";

const bindings = {
    parentEntityRef: "<",
};


const tabs = [
    {id: "SUMMARY", name: "Logical Flows"},
    {id: "LOGICAL_FLOW_SCROLL", name: "Logical Flows (Beta View)"},
    {id: "PHYSICAL", name: "Physical Flow Detail"},
    {id: "FLOW_CLASSIFICATION_RULES", name: "Flow Classification Rules"}
];

const modes = {
    EDIT: "EDIT",
    VIEW: "VIEW",
    BULK: "BULK"
}

const initialState = {
    FlowClassificationLegend,
    LogicalFlowScrollPanel,
    activeTab: null,
    changeUnits: [],
    dataTypeUsages: [],
    logicalFlows: [],
    logicalFlowDecorators: [],
    physicalFlows: [],
    physicalSpecifications: [],
    tags: [],
    visibility: {
        dataTab: 0,
        sourceDataRatings: false,
        editor: {
            logicalFlows: false,
            bulkLogicalFlows: false,
            bulkPhysicalFlows: false
        },
    },
    tabs,
    activeMode: modes.VIEW,
    lastTabId: tabs[0].id
};



function controller(serviceBroker) {

    const vm = initialiseData(this, initialState);

    function determineActiveTab() {
        serviceBroker
            .loadViewData(CORE_API.UserPreferenceStore.findAllForUser, [], {force: true})
            .then(prefs => {
                const lastTab = _.find(prefs.data, p => p.key === lastViewedFlowTabKey);
                const lastTabId = _.get(lastTab, ["value"], vm.tabs[0].id);
                vm.activeTab = _.find(vm.tabs, t => t.id === lastTabId);
            });
    }

    function setLastViewedTab() {
        serviceBroker
            .execute(
                CORE_API.UserPreferenceStore.saveForUser,
                [{key: lastViewedFlowTabKey, value: vm.activeTab.id}])
    }

    function loadData() {
        const selector = {
            entityReference: vm.parentEntityRef,
            scope: "EXACT"
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
                CORE_API.DataTypeDecoratorStore.findBySelector,
                [ selector, entity.LOGICAL_DATA_FLOW.key])
            .then(r => vm.logicalFlowDecorators = r.data);

        serviceBroker
            .loadViewData(
                CORE_API.FlowClassificationRuleStore.findByApp,
                [ vm.parentEntityRef.id ], {force: true})
            .then(r => {
                vm.flowClassificationRules = r.data;
            });

        serviceBroker
            .loadViewData(
                CORE_API.ChangeUnitStore.findBySelector,
                [mkSelectionOptions(vm.parentEntityRef)])
            .then(r => vm.changeUnits = r.data);

        serviceBroker
            .loadViewData(
                CORE_API.TagStore.findTagsByEntityKindAndTargetSelector,
                [entity.LOGICAL_DATA_FLOW.key, mkSelectionOptions(vm.parentEntityRef)])
            .then(r => vm.tags = r.data);

        serviceBroker
            .loadViewData(
                CORE_API.LogicalFlowStore.findPermissionsForParentRef,
                [vm.parentEntityRef])
            .then(r => {
                vm.canEdit = _.some(r.data, d => _.includes(["ADD", "UPDATE", "REMOVE"], d));
            });
    }

    vm.$onInit = () => {
        determineActiveTab();
        loadData();
    };

    vm.isAnyEditorVisible = () => {
        return vm.activeMode !== modes.VIEW;
    };

    vm.resetToViewMode = () => {
        setLastViewedTab();
        vm.activeMode = modes.VIEW;
        loadData();
    };

    vm.edit = () => {
        vm.activeMode = modes.EDIT;
    }

    vm.bulkLoad = () => {
        vm.activeMode = modes.BULK;
    }

}


controller.$inject = [
    "ServiceBroker"
];


const component = {
    template,
    bindings,
    controller
};


export default {
    component,
    id: "waltzDataFlowSection"
};

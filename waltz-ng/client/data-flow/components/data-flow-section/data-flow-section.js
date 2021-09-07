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
import FlowClassificationLegend from "../../../flow-classification-rule/components/svelte/FlowClassificationLegend.svelte";
import LogicalFlowScrollPanel from "../svelte/LogicalFlowScrollPanel.svelte"

const bindings = {
    parentEntityRef: "<",
};


const initialState = {
    FlowClassificationLegend,
    LogicalFlowScrollPanel,
    changeUnits: [],
    dataTypeUsages: [],
    logicalFlows: [],
    logicalFlowDecorators: [],
    physicalFlows: [],
    physicalSpecifications: [],
    tags: [],
    visibility: {
        dataTab: 0,
        logicalFlows: false, // this is the source data ratings panel, rename
        editor: {
            logicalFlows: false,
            bulkLogicalFlows: false,
            bulkPhysicalFlows: false
        },
    },
    tabs: [
        {id: "LOGICAL_FLOW_SCROLL", name: "Logical Flows (Beta View)"},
        {id: "SUMMARY", name: "Logical Flows"},
        {id: "PHYSICAL", name: "Physical Flow Detail"},
        {id: "FLOW_CLASSIFICATION_RULES", name: "Flow Classification Rules"}
    ]
};


function controller(serviceBroker) {
    const vm = initialiseData(this, initialState);

    function loadAdditionalAuthSourceData() {

        const orgUnitIds = _
            .chain(vm.flowClassificationRules)
            .map("parentReference")
            .filter({ kind: "ORG_UNIT"})
            .map("id")
            .uniq()
            .value();

        serviceBroker
            .loadAppData(
                CORE_API.OrgUnitStore.findByIds,
                [orgUnitIds])
            .then(r => {
                vm.orgUnits= r.data;
                vm.orgUnitsById = _.keyBy(r.data, "id");
            });
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
    }


    vm.$onInit = () => {
        loadData();
        vm.activeTab = vm.tabs[0];
    };

    vm.showTab = (idx) => {
        vm.visibility.dataTab = idx;
        if (idx === 2) {
            loadAdditionalAuthSourceData();
        }
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

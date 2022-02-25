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

import {initialiseData} from "../common";
import {CORE_API} from "../common/services/core-api-utils";
import template from "./orphans-view.html";
import toasts from "../svelte-stores/toast-store";
import {involvementStore} from "../svelte-stores/involvement-store";
import {entity} from "../common/services/enums/entity";

const initialState = {
    orphans: [],
};


function controller($q,
                    serviceBroker,
                    orphanStore) {

    const vm = initialiseData(this, initialState);


    vm.showOrphans = (values) => {
        vm.selectedOrphanValues = values;
    };


    const loadOrphans = () => {
        $q
            .all([
                orphanStore.findAppsWithNonExistentOrgUnits(),
                orphanStore.findOrphanMeasurableRatings(),
                orphanStore.findOrphanFlowClassificationRulesByOrgUnit(),
                orphanStore.findOrphanFlowClassificationRulesByApp(),
                orphanStore.findOrphanFlowClassificationRulesByDataType(),
                orphanStore.findOrphanChangeInitiatives(),
                orphanStore.findOrphanLogicalFlows(),
                orphanStore.findOrphanPhysicalFlows(),
                orphanStore.findOrphanAttestations()
            ])
            .then( ([apps,
                measurableRatings,
                authSourcesByOrgUnit,
                authSourcesByApp,
                authSourcesByDataType,
                changeInitiatives,
                logicalFlows,
                physicalFlows,
                attestations
            ]) => {
                vm.orphans = [
                    {description: "Applications referencing non-existent Org Units", values: apps},
                    {description: "Application Measurable Ratings mapping to non-existent Measurables or Apps", values: measurableRatings},
                    {description: "Flow Classification Rules with non-existent Org Unit", values: authSourcesByOrgUnit},
                    {description: "Flow Classification Rules with non-existent Application", values: authSourcesByApp},
                    {
                        description: "Flow Classification Rules with non-existent Data Type",
                        values: authSourcesByDataType
                    },
                    {description: "Change Initiatives with non-existent parent", values: changeInitiatives},
                    {description: "Logical Flows referencing non-existent applications", values: logicalFlows},
                    {
                        description: "Physical Flows referencing non-existent logical flows or specifications",
                        values: physicalFlows
                    },
                    {description: "Attestations referencing non-existent applications", values: attestations}
                ];
            });

    };

    const loadInvolvementOrphanCounts = () => {

        const eudaPromise = serviceBroker
            .loadViewData(CORE_API.InvolvementStore
                .countOrphanInvolvementsForKind, [entity.END_USER_APPLICATION.key]);

        const ciPromise = serviceBroker
            .loadViewData(CORE_API.InvolvementStore
                .countOrphanInvolvementsForKind, [entity.CHANGE_INITIATIVE.key]);

        $q
            .all([eudaPromise, ciPromise])
            .then(([endUserAppCount, changeInitiativeCount]) => {
                vm.involvementOrphanCounts = [
                    {
                        description: "Involvements referencing non-existent end user applications",
                        count: endUserAppCount.data
                    },
                    {
                        description: "Involvements referencing non-existent change initiatives",
                        count: changeInitiativeCount.data
                    }
                ];
            });

    };

    loadOrphans();
    loadInvolvementOrphanCounts();

    vm.cleanupLogicalFlows = () => {
        serviceBroker
            .execute(CORE_API.LogicalFlowStore.cleanupOrphans, [])
            .then(r => toasts.success(`Cleaned up ${r.data} flow/s`));
    };


    vm.cleanupSelfReferencingLogicalFlows = () => {
        serviceBroker
            .execute(CORE_API.LogicalFlowStore.cleanupSelfReferences, [])
            .then(r => toasts.success(`Cleaned up ${r.data} flow/s`));
    };


    vm.cleanupPhysicalFlows = () => {
        serviceBroker
            .execute(CORE_API.PhysicalFlowStore.cleanupOrphans, [])
            .then(r => toasts.success(`Cleaned up ${r.data} flow/s`));
    };


    vm.cleanupFlowClassificationRules = () => {
        serviceBroker
            .execute(CORE_API.FlowClassificationRuleStore.cleanupOrphans, [])
            .then(r => toasts.success(`Cleaned up ${r.data} auth sources/s`));
    };


    vm.cleanupAttestations = () => {
        serviceBroker
            .execute(CORE_API.AttestationInstanceStore.cleanupOrphans, [])
            .then(r => toasts.success(`Cleaned up ${r.data} attestations/s`));
    };


    vm.cleanupInvolvements = (entityKind) => {
        serviceBroker
            .execute(CORE_API.InvolvementStore.cleanupOrphansForKind, [entityKind])
            .then(r => toasts.success(`Cleaned up ${r.data} change initiative involvement/s`));
    };
}


controller.$inject = [
    "$q",
    "ServiceBroker",
    "OrphanStore"
];


export default {
    template,
    controller,
    controllerAs: "ctrl",
    bindToController: true,
    scope: {}
};



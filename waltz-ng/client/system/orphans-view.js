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
import template from './orphans-view.html';


const initialState = {
    orphans: [],
};


function controller($q,
                    notification,
                    orphanStore,
                    serviceBroker) {

    const vm = initialiseData(this, initialState);


    vm.showOrphans = (values) => {
        vm.selectedOrphanValues = values;
    };


    const loadOrphans = () => {
        $q
            .all([orphanStore.findAppsWithNonExistentOrgUnits(),
                orphanStore.findOrphanMeasurableRatings(),
                orphanStore.findOrphanAuthoritativeSourcesByOrgUnit(),
                orphanStore.findOrphanAuthoritativeSourcesByApp(),
                orphanStore.findOrphanAuthoritativeSourcesByDataType(),
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
                    {description: 'Applications referencing non-existent Org Units', values: apps},
                    {description: 'Application Measurable Ratings mapping to non-existent Measurables or Apps', values: measurableRatings},
                    {description: 'Authoritative Sources with non-existent Org Unit', values: authSourcesByOrgUnit},
                    {description: 'Authoritative Sources with non-existent Application', values: authSourcesByApp},
                    {description: 'Authoritative Sources with non-existent Data Type', values: authSourcesByDataType},
                    {description: 'Change Initiatives with non-existent parent', values: changeInitiatives},
                    {description: 'Logical Flows referencing non-existent applications', values: logicalFlows},
                    {description: 'Physical Flows referencing non-existent logical flows or specifications', values: physicalFlows},
                    {description: 'Attestations referencing non-existent applications', values: attestations}
                ];
            });

    };

    loadOrphans();

    vm.cleanupLogicalFlows = () => {
        serviceBroker
            .execute(CORE_API.LogicalFlowStore.cleanupOrphans, [])
            .then(r => notification.success(`Cleaned up ${r.data} flow/s`));
    };


    vm.cleanupSelfReferencingLogicalFlows = () => {
        serviceBroker
            .execute(CORE_API.LogicalFlowStore.cleanupSelfReferences, [])
            .then(r => notification.success(`Cleaned up ${r.data} flow/s`));
    };


    vm.cleanupPhysicalFlows = () => {
        serviceBroker
            .execute(CORE_API.PhysicalFlowStore.cleanupOrphans, [])
            .then(r => notification.success(`Cleaned up ${r.data} flow/s`));
    };


    vm.cleanupAuthSources = () => {
        serviceBroker
            .execute(CORE_API.AuthSourcesStore.cleanupOrphans, [])
            .then(r => notification.success(`Cleaned up ${r.data} auth sources/s`));
    };

    vm.cleanupAttestations = () => {
        serviceBroker
            .execute(CORE_API.AttestationInstanceStore.cleanupOrphans, [])
            .then(r => notification.success(`Cleaned up ${r.data} attestations/s`));
    };
}


controller.$inject = [
    '$q',
    'Notification',
    'OrphanStore',
    'ServiceBroker'
];


export default {
    template,
    controller,
    controllerAs: 'ctrl',
    bindToController: true,
    scope: {}
};



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



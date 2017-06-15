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

import {initialiseData} from "../common";
import {CORE_API} from "../common/services/core-api-utils";


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
                orphanStore.findOrphanLogicalFlows()
            ])
            .then( ([apps,
                measurableRatings,
                authSourcesByOrgUnit,
                authSourcesByApp,
                authSourcesByDataType,
                changeInitiatives,
                logicalFlows
            ]) => {
                const orphans = [
                    {description: 'Applications referencing non-existent Org Units', values: apps},
                    {description: 'Application Measurable Ratings mapping to non-existent Measurables or Apps', values: measurableRatings},
                    {description: 'Authoritative Sources with non-existent Org Unit', values: authSourcesByOrgUnit},
                    {description: 'Authoritative Sources with non-existent Application', values: authSourcesByApp},
                    {description: 'Authoritative Sources with non-existent Data Type', values: authSourcesByDataType},
                    {description: 'Change Initiatives with non-existent parent', values: changeInitiatives},
                    {description: 'Logical Flows referencing non-existent applications', values: logicalFlows}
                ];
                vm.orphans = orphans;
            });

    };

    loadOrphans();

    vm.cleanupLogicalFlows = () => {
        serviceBroker
            .execute(CORE_API.LogicalFlowStore.cleanupOrphans, [])
            .then(r => notification.success(`Cleaned up ${r.data} flow/s`));
    };

    vm.cleanupAuthSources = () => {
        serviceBroker
            .execute(CORE_API.AuthSourcesStore.cleanupOrphans, [])
            .then(r => notification.success(`Cleaned up ${r.data} auth sources/s`));
    };
}


controller.$inject = [
    '$q',
    'Notification',
    'OrphanStore',
    'ServiceBroker'
];


export default {
    template: require('./orphans-view.html'),
    controller,
    controllerAs: 'ctrl',
    bindToController: true,
    scope: {}
};



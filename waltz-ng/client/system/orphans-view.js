/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016  Khartec Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import {initialiseData} from "../common";


const initialState = {
    orphans: [],
};


function controller($q,
                    orphanStore) {

    const vm = initialiseData(this, initialState);


    vm.showOrphans = (values) => {
        vm.selectedOrphanValues = values;
    };


    const loadOrphans = () => {
        $q
            .all([orphanStore.findAppsWithNonExistentOrgUnits(),
                orphanStore.findOrphanAppCaps(),
                orphanStore.findOrphanAuthoritativeSourcesByOrgUnit(),
                orphanStore.findOrphanAuthoritativeSourcesByApp(),
                orphanStore.findOrphanAuthoritativeSourcesByDataType(),
                orphanStore.findOrphanChangeInitiatives(),
                orphanStore.findOrphanLogicalFlows()
            ])
            .then( ([apps,
                appCaps,
                authSourcesByOrgUnit,
                authSourcesByApp,
                authSourcesByDataType,
                changeInitiatives,
                logicalFlows
            ]) => {
                const orphans = [
                    {description: 'Applications referencing non-existent Org Units', values: apps},
                    {description: 'Application Capabilities mapping to non-existent Functions or Apps', values: appCaps},
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
}


controller.$inject = [
    '$q',
    'OrphanStore'
];


export default {
    template: require('./orphans-view.html'),
    controller,
    controllerAs: 'ctrl',
    bindToController: true,
    scope: {}
};


